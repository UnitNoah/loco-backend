package com.loco.loco_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
import com.loco.loco_api.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @MockBean(name = "jpaMappingContext")
    JpaMetamodelMappingContext jpaMappingContext;

    @Autowired private MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean private RoomService roomService;

    // 방 상세 조회
    @Test
    void getRoom_returns200WithBody() throws Exception {
        var resp = new RoomResponse(1L, "스터디룸", "조용한 곳", true,
                "https://cdn.example.com/img.png", 42L, "ABCD1234");
        when(roomService.getDetail(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/rooms/{roomId}", 1L).with(user("tester").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("스터디룸"))
                .andExpect(jsonPath("$.data.description").value("조용한 곳"))
                .andExpect(jsonPath("$.data.is_private").value(true))
                .andExpect(jsonPath("$.data.thumbnail").value("https://cdn.example.com/img.png"))
                .andExpect(jsonPath("$.data.host_id").value(42))
                .andExpect(jsonPath("$.data.invite_code").value("ABCD1234"));
    }

    @Test
    void getRoom_notFound_returns404() throws Exception {
        when(roomService.getDetail(999L)).thenThrow(new CustomException(ErrorCode.ROOM_NOT_FOUND));

        mvc.perform(get("/api/v1/rooms/{roomId}", 999L).with(user("tester").roles("USER")))
                .andExpect(status().isNotFound());
    }

    // 방 생성
    @Test
    void createRoom_returns200WithBody() throws Exception {
        var req  = new RoomCreateRequest("스터디룸","조용한 곳", true, "https://cdn.example.com/img.png");
        var resp = new RoomResponse(1L, "스터디룸", "조용한 곳", true, "https://cdn.example.com/img.png", 42L, "ABCD1234" );
        when(roomService.create(any(), eq(42L))).thenReturn(resp);


        mvc.perform(post("/api/v1/rooms")
                        .with(user("tester").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("hostId", "42")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("스터디룸"))
                .andExpect(jsonPath("$.data.invite_code").isNotEmpty());
    }


    @Test
    void createRoom_validationError_returns400() throws Exception {
        var invalid = new RoomCreateRequest("", "x", true, null);

        mvc.perform(post("/api/v1/rooms")
                        .with(user("tester").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("hostId", "42")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}