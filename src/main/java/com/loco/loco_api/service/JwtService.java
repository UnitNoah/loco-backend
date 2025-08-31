package com.loco.loco_api.service;

import org.springframework.security.oauth2.jose.jws.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

  private final JwtEncoder encoder;

  public JwtService(JwtEncoder encoder) { this.encoder = encoder; }

  public String issueAccessToken(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();
    JwtClaimsSet claimSet = JwtClaimsSet.builder()
            .issuer("https://api.loco.com")
            .subject(subject)                 // 예: "google 1234567890"
            .issuedAt(now)
            .expiresAt(now.plusSeconds(900))  // 15분
            .claim("roles", List.of("ROLE_USER"))
            .claims(c -> c.putAll(claims))
            .build();

    // 헤더에 kid/알고리즘 명시
    JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
            .type("JWT")
            .build();

    return encoder.encode(JwtEncoderParameters.from(header, claimSet)).getTokenValue();
  }
}
