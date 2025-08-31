// src/test/java/.../SecurityIntegrationTest.java
package com.loco.loco_api;

import com.loco.loco_api.config.SecurityConfig;
import com.loco.loco_api.controller.TestAuthController;
import com.loco.loco_api.service.CustomOAuth2UserService;
import com.loco.loco_api.service.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestAuthController.class)
@Import(SecurityConfig.class)  // 우리가 만든 Security 설정 사용
class SecurityIntegrationTest {

  @Autowired MockMvc mvc;

  @MockBean JwtDecoder jwtDecoder;                 // 검증을 모킹해 토큰을 수용
  @MockBean CustomOAuth2UserService oAuth2UserService; // SecurityConfig 의존성 해결용
  @MockBean JwtService jwtService;                 // 성공 핸들러 의존성 해결용(여기선 미사용)

  private Jwt jwt(String token, List<String> roles) {
    Map<String, Object> headers = Map.of("alg", "RS256", "kid", "test");
    Map<String, Object> claims = Map.of(
            "sub", "google 1234567890",
            "name", "Test User",
            "roles", roles,
            "aud", List.of("loco-web"),
            "iss", "https://api.loco.com"
    );
    Instant now = Instant.now();
    return new Jwt(token, now, now.plusSeconds(600), headers, claims);
  }

  @Test
  void deny_when_no_token() throws Exception {
    mvc.perform(get("/me"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void allow_when_cookie_token_present() throws Exception {
    // JwtDecoder가 "t1" 토큰을 유효한 Jwt로 반환하도록 설정
    when(jwtDecoder.decode("t1")).thenReturn(jwt("t1", List.of("ROLE_USER")));

    mvc.perform(get("/me").cookie(new Cookie("access_token", "t1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").value("google 1234567890"))
            .andExpect(jsonPath("$.name").value("Test User"))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
  }

  @Test
  void authorities_mapping_from_roles_claim() throws Exception {
    when(jwtDecoder.decode("t2")).thenReturn(jwt("t2", List.of("ROLE_USER")));

    mvc.perform(get("/authorities").cookie(new Cookie("access_token", "t2")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
  }

  @Test
  void cors_preflight_ok() throws Exception {
    mvc.perform(options("/me")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
  }
}
