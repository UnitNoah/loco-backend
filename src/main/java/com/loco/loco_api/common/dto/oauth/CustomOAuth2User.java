package com.loco.loco_api.common.dto.oauth;

import com.loco.loco_api.common.dto.user.request.UserDTO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

  @Getter
  private final UserDTO user;                   // provider, oauthId, email, name, role
  private final Map<String, Object> attributes; // 원본 attributes

  public CustomOAuth2User(UserDTO user) {
    this(user, Map.of());
  }

  public CustomOAuth2User(UserDTO user, Map<String, Object> attributes) {
    this.user = user;
    this.attributes = attributes != null ? attributes : Map.of();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public List<? extends GrantedAuthority> getAuthorities() {
    String role = user.getRole() != null ? user.getRole() : "ROLE_USER";
    return List.of(new SimpleGrantedAuthority(role));
  }

  /**
   * Spring Security에서 내부적으로 식별자로 쓰는 값.
   * 여기서는 provider + "_" + oauthId 조합을 반환.
   */
  @Override
  public String getName() {
    return getSub();
  }

  /** === 편의 메서드 === */

  /** JWT sub 값: provider + "_" + oauthId */
  public String getSub() {
    return getProvider() + "_" + getOauthId();
  }

  /** 화면에 표시할 닉네임/이름 */
  public String getDisplayName() {
    return user.getName();
  }

  /** 권한 */
  public String getRole() {
    return user.getRole();
  }

  /** provider (google/naver/kakao) */
  public String getProvider() {
    return user.getProvider();
  }

  /** 소셜 제공자의 고유 userId */
  public String getOauthId() {
    return user.getOauthId();
  }

  /** 이메일 (동의 안하면 null일 수 있음) */
  public String getEmail() {
    return user.getEmail();
  }
}

