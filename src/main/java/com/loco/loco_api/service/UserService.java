package com.loco.loco_api.service;

import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  /**
   * provider + oauthId 기반으로 사용자 조회
   *
   * @param provider 소셜 로그인 제공자 (google, naver, kakao 등)
   * @param oauthId  제공자에서 발급한 유저 고유 ID
   * @return Optional<UserEntity>
   */
  public Optional<UserEntity> findByProviderAndOauthId(String provider, String oauthId) {
    return userRepository.findByProviderAndOauthId(provider, oauthId);
  }

  // JWT에서 sub 파싱 + DB 조회까지 한 번에 처리
  public UserEntity getCurrentUser(Jwt jwt) {
    String[] parts = jwt.getSubject().split(" ");
    String provider = parts[0];
    String oauthId  = parts[1];

    UserEntity user = userRepository.findByProviderAndOauthId(provider, oauthId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getDeletedAt() != null) {
      throw new RuntimeException("탈퇴한 회원입니다.");
    }

    return user;
  }
}
