package com.loco.loco_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.service.CustomOAuth2UserService;
import com.loco.loco_api.service.JwtService;
import com.loco.loco_api.service.RoomService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RoomControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockBean
    JwtDecoder jwtDecoder;
    @MockBean
    CustomOAuth2UserService oAuth2UserService;
    @MockBean
    JwtService jwtService;
    @MockBean
    RoomService roomService;

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
    void createRoom_201_when_authenticated_and_valid() throws Exception {
        when(jwtDecoder.decode("t1")).thenReturn(jwt("t1", List.of("ROLE_USER")));

        RoomResponse resp = new RoomResponse(42L, "카공하기 좋은 카페", "나만 알기 아까운", true, "https://cdn.example.com/rooms/abc.png", 1L, "ABCDE123");
        when(roomService.create(any(RoomCreateRequest.class), eq(1L))).thenReturn(resp);

        RoomCreateRequest req = new RoomCreateRequest(
                "카공하기 좋은 카페",
                "나만 알기 아까운",
                true,
                "https://cdn.example.com/rooms/abc.png"
        );

        mvc.perform(post("/api/v1/rooms?hostId=1").with(csrf())
                .cookie(new Cookie("access_token", "t1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.name").value("카공하기 좋은 카페"))
                .andExpect(jsonPath("$.data.description").value("나만 알기 아까운"))
                .andExpect(jsonPath("$.data.is_private").value(true))
                .andExpect(jsonPath("$.data.thumbnail").value("https://cdn.example.com/rooms/abc.png"))
                .andExpect(jsonPath("$.data.host_id").value(1))
                .andExpect(jsonPath("$.data.invite_code").value("ABCDE123"));

        verify(roomService).create(any(RoomCreateRequest.class), eq(1L));
    }

    @Test
    void createRoom_400_when_name_missing() throws Exception {
        when(jwtDecoder.decode("t2")).thenReturn(jwt("t2", List.of("ROLE_USER")));

        String badJson = """
      { "name": "  ", "description": "x", "is_private": true, "thumbnail": null }
      """;

        mvc.perform(post("/api/v1/rooms?hostId=1").with(csrf())
                .cookie(new Cookie("access_token", "t2"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoom_401_when_unauthenticated() throws Exception {
        RoomCreateRequest req = new RoomCreateRequest("제목", null, null, null);
        mvc.perform(post("/api/v1/rooms?hostId=1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
