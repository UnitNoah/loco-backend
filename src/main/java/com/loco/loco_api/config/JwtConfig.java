package com.loco.loco_api.config;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.interfaces.*;

@Configuration
public class JwtConfig {

  @Bean
  public RSAKey rsaJwk(KeyPair keyPair) {
    RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey pri = (RSAPrivateKey) keyPair.getPrivate();
    return new RSAKey.Builder(pub)
            .privateKey(pri)
            .keyID("kid-2025-08-31") // 필수: 로테이션 대비
            .build();
  }

  @Bean
  public JwtEncoder jwtEncoder(RSAKey rsaJwk) {
    JWKSet jwkSet = new JWKSet(rsaJwk);
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(jwkSet));
  }
}
