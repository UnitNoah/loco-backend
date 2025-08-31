package com.loco.loco_api.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

// 첫 요청에서 CSRF 토큰을 발급/노출해서 프론트가 헤더로 보낼 수 있게 함
@RestController
public class CsrfController {

  @GetMapping("/csrf-token")
  public Map<String, String> token(CsrfToken token) {
    // 이 호출로 CSRF 토큰이 강제로 로드되며,
    // CookieCsrfTokenRepository가 XSRF-TOKEN 쿠키도 내려줌.
    return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
  }
}
