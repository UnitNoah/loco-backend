package com.loco.loco_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.Base64;

@Configuration
@Profile("prod")
public class RsaKeyConfigProd {

  @Bean
  public KeyPair keyPair(
          @Value("classpath:jwt/private.pem") Resource privatePem,
          @Value("classpath:jwt/public.pem") Resource publicPem
  ) throws Exception {
    String priv = new String(privatePem.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replaceAll("-----BEGIN (.*)-----", "")
            .replaceAll("-----END (.*)-----", "")
            .replaceAll("\\s", "");
    String pub = new String(publicPem.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replaceAll("-----BEGIN (.*)-----", "")
            .replaceAll("-----END (.*)-----", "")
            .replaceAll("\\s", "");

    KeyFactory kf = KeyFactory.getInstance("RSA");
    RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(
            new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priv)));
    RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(
            new X509EncodedKeySpec(Base64.getDecoder().decode(pub)));

    return new KeyPair(publicKey, privateKey);
  }
}
