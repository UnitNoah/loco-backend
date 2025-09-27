package com.loco.loco_api.controller;

import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 끝나면 롤백
class UserIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired
  UserRepository userRepository;

  @Test
  void profile_fromPostgres_success() throws Exception {
    // given: 실제 PostgreSQL DB에 데이터 저장
    userRepository.save(UserEntity.builder()
            .provider("google")
            .oauthId("1234567890")
            .nickname("이안")
            .profileImageUrl("https://cdn.test.com/profile.jpg")
            .email("test@test.com")
            .build());

    // when & then
    mockMvc.perform(get("/api/v1/users/profile")
                    .with(jwt().jwt(jwt -> jwt
                            .claim("sub", "google 1234567890")
                            .claim("roles", List.of("ROLE_USER"))
                    )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("이안"));
  }
}

