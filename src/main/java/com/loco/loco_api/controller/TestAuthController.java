// src/main/java/.../controller/TestAuthController.java
package com.loco.loco_api.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TestAuthController {

  // 보호 리소스: 인증 필요. JWT의 클레임을 그대로 확인
  @GetMapping("/me")
  public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
    return Map.of(
            "sub", jwt.getSubject(),
            "name", jwt.getClaimAsString("name"),
            "roles", jwt.getClaimAsStringList("roles")
    );
  }

  // 현재 인증의 권한을 확인(ROLE 매핑 확인용)
  @GetMapping("/authorities")
  public Map<String, Object> authorities(Authentication authentication) {
    var auths = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
    return Map.of("authorities", auths);
  }
}
