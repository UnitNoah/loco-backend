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

  public JwtService(JwtEncoder encoder) {
    this.encoder = encoder;
  }

  public String issueAccessToken(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();
    JwtClaimsSet claimSet = JwtClaimsSet.builder()
            .issuer("https://api.loco.com")
            .subject(subject)
            .audience(List.of("loco-web"))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(900))
            .claim("roles", List.of("ROLE_USER"))
            // ↓ email, nickname 같이 들어가도록 claims 확장
            .claims(c -> c.putAll(claims))
            .build();

    JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
            .type("JWT")
            .build();

    return encoder.encode(JwtEncoderParameters.from(header, claimSet)).getTokenValue();
  }
}
