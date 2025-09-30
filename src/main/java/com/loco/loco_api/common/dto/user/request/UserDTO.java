package com.loco.loco_api.common.dto.user.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
  private String role;         // 권한
  private String name;         // 닉네임/표시명
  private String provider;     // google/naver/kakao
  private String oauthId;      // 소셜 제공자의 고유 ID
  private String email;        // 사용자 이메일 (nullable 가능성 있음)
  private String profileImage; // 프로필 이미지 URL (nullable 가능성 있음)
}
