package com.loco.loco_api.common.dto.oauth;

import com.loco.loco_api.common.dto.user.UserDTO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

  // 필요시 전체 DTO 접근
  @Getter
  private final UserDTO user;                       // username, name, role, email 등
  private final Map<String, Object> attributes;     // 원본 OAuth2 attributes(옵션)

  public CustomOAuth2User(UserDTO user) {
    this(user, Map.of());
  }

  public CustomOAuth2User(UserDTO user, Map<String, Object> attributes) {
    this.user = user;
    this.attributes = attributes != null ? attributes : Map.of();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes; // null 반환 금지
  }

  @Override
  public List<? extends GrantedAuthority> getAuthorities() {
    String role = user.getRole() != null ? user.getRole() : "ROLE_USER";
    return List.of(new SimpleGrantedAuthority(role));
  }

  /** OAuth2User.getName(): 고유 식별자 반환 권장(표시명 아님) */
  @Override
  public String getName() {
    return user.getUsername(); // 예: "google 1234567890"
  }

  // 편의 메서드
  public String getUsername() { return user.getUsername(); }
  public String getDisplayName() { return user.getName(); }  // 화면 표기용
  public String getRole() { return user.getRole(); }         // ← 추가
}
