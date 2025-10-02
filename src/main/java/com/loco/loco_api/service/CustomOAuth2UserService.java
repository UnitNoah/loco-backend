package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.oauth.CustomOAuth2User;
import com.loco.loco_api.common.dto.oauth.GoogleResponse;
import com.loco.loco_api.common.dto.oauth.NaverResponse;
import com.loco.loco_api.common.dto.oauth.OAuth2Response;
import com.loco.loco_api.common.dto.user.request.UserDTO;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService  {

  private final UserRepository userRepository;
  private final OidcUserService oidcUserService = new OidcUserService(); // 구글용 delegate

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User;

    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    if ("google".equals(registrationId)) {
      // 구글은 OIDC 기반
      oAuth2User = oidcUserService.loadUser((OidcUserRequest) userRequest);
    } else {
      // 네이버, 카카오는 OAuth2 기반
      oAuth2User = super.loadUser(userRequest);
    }

    // 공통 처리
    OAuth2Response oAuth2Response = switch (registrationId) {
      case "google" -> new GoogleResponse(oAuth2User.getAttributes());
      case "naver"  -> new NaverResponse(oAuth2User.getAttributes());
      default -> throw new OAuth2AuthenticationException("Unsupported provider");
    };

    String provider = oAuth2Response.getProvider();
    String oauthId  = oAuth2Response.getProviderId();
    String name     = oAuth2Response.getName();
    String email    = oAuth2Response.getEmail();
    String imageUrl = oAuth2Response.getProfileImageUrl();

    UserEntity user = userRepository.findByProviderAndOauthId(provider, oauthId)
            .map(u -> {
              if (u.getDeletedAt() != null) {
                throw new OAuth2AuthenticationException("탈퇴한 회원입니다.");
              }
              u.updateProfile(name, imageUrl, email);
              return u;
            })
            .orElseGet(() -> userRepository.save(
                    UserEntity.builder()
                            .provider(provider)
                            .oauthId(oauthId)
                            .email(email)
                            .nickname(name)
                            .profileImageUrl(imageUrl)
                            .build()
            ));

    log.info("OAuth2 로그인 성공: provider={}, oauthId={}, email={}", provider, oauthId, email);

    UserDTO dto = new UserDTO();
    dto.setProvider(provider);
    dto.setOauthId(oauthId);
    dto.setEmail(user.getEmail());
    dto.setName(user.getNickname());
    dto.setRole("ROLE_USER");
    dto.setProfileImage(user.getProfileImageUrl());

    return new CustomOAuth2User(dto, oAuth2User.getAttributes());
  }

}

