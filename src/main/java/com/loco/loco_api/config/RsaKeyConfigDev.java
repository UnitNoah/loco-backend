package com.loco.loco_api.config;

import org.springframework.context.annotation.*;
import java.security.*;

@Configuration
@Profile({"default","dev"}) // 운영 제외
public class RsaKeyConfigDev {

  @Bean
  public KeyPair keyPair() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    return kpg.generateKeyPair();
  }
}