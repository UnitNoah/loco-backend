package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.oauth.GoogleResponse;
import com.loco.loco_api.common.dto.oauth.NaverResponse;
import com.loco.loco_api.common.dto.oauth.OAuth2Response;
import com.loco.loco_api.common.dto.oauth.CustomOAuth2User;
import com.loco.loco_api.common.dto.user.request.UserDTO;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @org.springframework.transaction.annotation.Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google/naver

    OAuth2Response oAuth2Response = switch (registrationId) {
      case "google" -> new GoogleResponse(oAuth2User.getAttributes());
      case "naver"  -> new NaverResponse(oAuth2User.getAttributes());
      // case "kakao"  -> new KakaoResponse(oAuth2User.getAttributes());
      default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    };

    String provider = oAuth2Response.getProvider();
    String oauthId  = oAuth2Response.getProviderId();
    if (provider == null || provider.isBlank() || oauthId == null || oauthId.isBlank()) {
      throw new OAuth2AuthenticationException("Missing provider/providerId");
    }

    String name     = oAuth2Response.getName();            // nullable
    String imageUrl = oAuth2Response.getProfileImageUrl(); // nullable
    String email    = oAuth2Response.getEmail();           // nullable

    // 존재 여부 확인 후 업데이트/생성
    UserEntity user = userRepository
            .findByProviderAndOauthId(provider, oauthId)
            .map(u -> { u.updateProfile(name, imageUrl, email); return u; })
            .orElseGet(() -> userRepository.save(
                    UserEntity.builder()
                            .provider(provider)
                            .oauthId(oauthId)
                            .email(email)              // << 신규 생성 시 이메일 저장 (누락 금지)
                            .nickname(name)
                            .profileImageUrl(imageUrl)
                            .build()
            ));

    // 표시용 이름 폴백
    String displayName =
            (user.getNickname() != null && !user.getNickname().isBlank()) ? user.getNickname()
                    : (user.getEmail() != null && !user.getEmail().isBlank())       ? user.getEmail()
                    : provider + " " + oauthId;

    UserDTO userDTO = new UserDTO();
    userDTO.setUsername(provider + " " + oauthId);  // 내부 식별용
    userDTO.setName(displayName);
    userDTO.setRole("ROLE_USER");

    return new CustomOAuth2User(userDTO);
  }
}

