package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.oauth.CustomOAuth2User;
import com.loco.loco_api.common.dto.oauth.GoogleResponse;
import com.loco.loco_api.common.dto.oauth.NaverResponse;
import com.loco.loco_api.common.dto.oauth.OAuth2Response;
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
    String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google/naver/kakao

    OAuth2Response oAuth2Response = switch (registrationId) {
      case "google" -> new GoogleResponse(oAuth2User.getAttributes());
      case "naver"  -> new NaverResponse(oAuth2User.getAttributes());
      // case "kakao"  -> new KakaoResponse(oAuth2User.getAttributes());
      default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    };

    String provider = oAuth2Response.getProvider();       // google/naver/kakao
    String oauthId  = oAuth2Response.getProviderId();     // 소셜 유저 ID
    if (provider == null || provider.isBlank() || oauthId == null || oauthId.isBlank()) {
      throw new OAuth2AuthenticationException("Missing provider/providerId");
    }

    String name     = oAuth2Response.getName();            // nullable
    String imageUrl = oAuth2Response.getProfileImageUrl(); // nullable
    String email    = oAuth2Response.getEmail();           // nullable

    // === 사용자 조회 or 신규 생성 ===
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

    // 표시용 이름 (fallback: 닉네임 → 이메일 → provider_id)
    String displayName =
            (user.getNickname() != null && !user.getNickname().isBlank()) ? user.getNickname()
                    : (user.getEmail() != null && !user.getEmail().isBlank())     ? user.getEmail()
                    : provider + "_" + oauthId;

    // === Security Context로 넘길 DTO 생성 ===
    UserDTO userDTO = new UserDTO();
    userDTO.setProvider(provider);
    userDTO.setOauthId(oauthId);
    userDTO.setEmail(user.getEmail());
    userDTO.setName(displayName);
    userDTO.setRole("ROLE_USER"); // 추후 DB role 컬럼 매핑 가능

    return new CustomOAuth2User(userDTO, oAuth2User.getAttributes());
  }
}

