package com.loco.loco_api.controller;

import com.nimbusds.jose.jwk.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class JwksController {
  private final RSAKey rsaJwk;
  public JwksController(RSAKey rsaJwk) { this.rsaJwk = rsaJwk; }

  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> keys() {
    // 공개키만 노출
    return new JWKSet(rsaJwk.toPublicJWK()).toJSONObject();
  }
}

