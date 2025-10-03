package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.user.request.UserUpdateRequest;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
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
    String sub = jwt.getSubject();
    if (sub == null || !sub.contains("_")) {
      throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
    }

    String[] parts = sub.split("_", 2);
    String provider = parts[0];
    String oauthId  = parts[1];

    UserEntity user = userRepository.findByProviderAndOauthId(provider, oauthId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (user.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
    }

    return user;
  }


  /**
   * 회원 정보 수정
   * @param jwt
   * @param request
   * @return
   */
  @Transactional
  public UserEntity updateUser(Jwt jwt, UserUpdateRequest request) {
    UserEntity user = getCurrentUser(jwt);
    if (user == null) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 탈퇴 회원 수정 불가
    if (user.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
    }

    // 닉네임,이미지 유효성 검사
    if ((request.nickname() == null || request.nickname().isBlank())
            && (request.profileImageUrl() == null || request.profileImageUrl().isBlank())) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }

    if (request.nickname().length() > 20) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
    // TODO: 프로필 이미지 URL 형식 검사 추가해야함
//    if () {
//      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
//    }

    user.updateProfile(request.nickname(), request.profileImageUrl());
    return userRepository.save(user);
  }

  /**
   * 회원 탈퇴(soft delete)
   * @param jwt
   */
  @Transactional
  public void deleteUser(Jwt jwt) {
    UserEntity user = getCurrentUser(jwt);
    if (user == null) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 이미 탈퇴한 회원
    if (user.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
    }

    user.delete();
    userRepository.save(user);
  }

}
