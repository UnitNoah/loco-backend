package com.loco.loco_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.request.RoomUpdateRequest;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @MockBean(name = "jpaMappingContext")
    JpaMetamodelMappingContext jpaMappingContext;

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private RoomService roomService;

    private RequestPostProcessor auth() { return user("tester").roles("USER"); }

    // ---------- 단건 조회 (공개) ----------
    @Test
    void getPublicRoom_returns200WithBody() throws Exception {
        var resp = new RoomResponse(
                1L, "스터디룸", "조용한 곳", true,
                "https://cdn.example.com/img.png", 42L, "ABCD1234",
                "홍길동", "https://cdn.example.com/users/u42.png"
        );
        when(roomService.getPublicDetail(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/rooms/public/{roomId}", 1L).with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("스터디룸"))
                .andExpect(jsonPath("$.data.description").value("조용한 곳"))
                .andExpect(jsonPath("$.data.is_private").value(true))
                .andExpect(jsonPath("$.data.thumbnail").value("https://cdn.example.com/img.png"))
                .andExpect(jsonPath("$.data.host_id").value(42))
                .andExpect(jsonPath("$.data.invite_code").value("ABCD1234"))
                .andExpect(jsonPath("$.data.host_nickname").value("홍길동"))
                .andExpect(jsonPath("$.data.host_profile_image_url").value("https://cdn.example.com/users/u42.png"));
    }

    @Test
    void getPublicRoom_notFound_returns404() throws Exception {
        when(roomService.getPublicDetail(999L)).thenThrow(new CustomException(ErrorCode.ROOM_NOT_FOUND));

        mvc.perform(get("/api/v1/rooms/public/{roomId}", 999L).with(auth()))
                .andExpect(status().isNotFound());
    }

    // ---------- 단건 조회 (비공개) ----------
    @Test
    void getPrivateRoom_returns200WithBody() throws Exception {
        var resp = new RoomResponse(
                2L, "비밀방", "설명", true,
                "https://cdn.example.com/secret.png", 77L, "SECR3T",
                "비밀호스트", "https://cdn.example.com/users/u77.png"
        );
        when(roomService.getPrivateDetail(2L)).thenReturn(resp);

        mvc.perform(get("/api/v1/rooms/private/{roomId}", 2L).with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("비밀방"))
                .andExpect(jsonPath("$.data.is_private").value(true))
                .andExpect(jsonPath("$.data.host_id").value(77))
                .andExpect(jsonPath("$.data.invite_code").value("SECR3T"))
                .andExpect(jsonPath("$.data.host_nickname").value("비밀호스트"))
                .andExpect(jsonPath("$.data.host_profile_image_url").value("https://cdn.example.com/users/u77.png"));
    }

    @Test
    void getPrivateRoom_notFound_returns404() throws Exception {
        when(roomService.getPrivateDetail(404L)).thenThrow(new CustomException(ErrorCode.ROOM_NOT_FOUND));

        mvc.perform(get("/api/v1/rooms/private/{roomId}", 404L).with(auth()))
                .andExpect(status().isNotFound());
    }

    // ---------- 공개방 목록 ----------
    @Test
    void listPublic_returns200WithArray() throws Exception {
        var r1 = new RoomResponse(3L,"공개3","d",false,"t",10L,"C3","닉","u");
        var r2 = new RoomResponse(2L,"공개2","d",false,"t",10L,"C2","닉","u");
        var r3 = new RoomResponse(1L,"공개1","d",false,"t",10L,"C1","닉","u");
        when(roomService.listPublic()).thenReturn(List.of(r1, r2, r3));

        mvc.perform(get("/api/v1/rooms/public").with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].is_private").value(false))
                .andExpect(jsonPath("$.data[1].is_private").value(false))
                .andExpect(jsonPath("$.data[2].is_private").value(false))
                .andExpect(jsonPath("$.data[0].id").value(3))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[2].id").value(1));
    }

    @Test
    void listPublic_empty_returnsEmptyArray() throws Exception {
        when(roomService.listPublic()).thenReturn(List.of());

        mvc.perform(get("/api/v1/rooms/public").with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ---------- 비공개방 목록 ----------
    @Test
    void listPrivate_returns200WithArray() throws Exception {
        var r1 = new RoomResponse(5L,"비공개5","d",true,"t",10L,"X5","닉","u");
        var r2 = new RoomResponse(4L,"비공개4","d",true,"t",10L,"X4","닉","u");
        when(roomService.listPrivate()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/v1/rooms/private").with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].is_private").value(true))
                .andExpect(jsonPath("$.data[1].is_private").value(true))
                .andExpect(jsonPath("$.data[0].id").value(5))
                .andExpect(jsonPath("$.data[1].id").value(4));
    }

    // ---------- 생성 ----------
    @Test
    void createRoom_returns200WithBody() throws Exception {
        var req  = new RoomCreateRequest("스터디룸","조용한 곳", true, "https://cdn.example.com/img.png");
        var resp = new RoomResponse(1L, "스터디룸", "조용한 곳", true,
                "https://cdn.example.com/img.png", 42L, "ABCD1234", "홍길동", "https://cdn.example.com/users/u42.png");
        when(roomService.create(any(), eq(42L))).thenReturn(resp);

        mvc.perform(post("/api/v1/rooms")
                        .with(auth())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("hostId", "42")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("스터디룸"))
                .andExpect(jsonPath("$.data.invite_code").isNotEmpty())
                .andExpect(jsonPath("$.data.host_nickname").value("홍길동"))
                .andExpect(jsonPath("$.data.host_profile_image_url").value("https://cdn.example.com/users/u42.png"));
    }

    @Test
    void createRoom_validationError_returns400() throws Exception {
        var invalid = new RoomCreateRequest("", "x", true, null);

        mvc.perform(post("/api/v1/rooms")
                        .with(auth())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("hostId", "42")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ---------- 수정 ----------
    @Test
    void update_success_returns200WithBody() throws Exception {
        var req = new RoomUpdateRequest("새이름", "새설명", false, "https://cdn.new/img.png");
        var resp = new RoomResponse(1L, "새이름", "새설명", false,
                "https://cdn.new/img.png", 42L, "ABC1234", "홍길동", "https://cdn.example.com/users/u42.png");

        when(roomService.update(eq(1L), eq(42L), any(RoomUpdateRequest.class))).thenReturn(resp);

        mvc.perform(patch("/api/v1/rooms/{roomId}", 1L)
                        .with(auth())
                        .with(csrf())
                        .param("requesterId", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("새이름"))
                .andExpect(jsonPath("$.data.is_private").value(false))
                .andExpect(jsonPath("$.data.thumbnail").value("https://cdn.new/img.png"))
                .andExpect(jsonPath("$.data.host_nickname").value("홍길동"))
                .andExpect(jsonPath("$.data.host_profile_image_url").value("https://cdn.example.com/users/u42.png"));
    }

    @Test
    void update_forbidden_returns403() throws Exception {
        var req = new RoomUpdateRequest("x", null, null, null);
        when(roomService.update(eq(1L), eq(77L), any(RoomUpdateRequest.class)))
                .thenThrow(new CustomException(ErrorCode.ROOM_NOT_HOST));

        mvc.perform(patch("/api/v1/rooms/{roomId}", 1L)
                        .with(auth())
                        .with(csrf())
                        .param("requesterId", "77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        var req = new RoomUpdateRequest("room", "desc", null, null);
        when(roomService.update(eq(999L), eq(42L), any(RoomUpdateRequest.class)))
                .thenThrow(new CustomException(ErrorCode.ROOM_NOT_FOUND));

        mvc.perform(patch("/api/v1/rooms/{roomId}", 999L)
                        .with(auth())
                        .with(csrf())
                        .param("requesterId", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ---------- 삭제 ----------
    @Test
    void delete_success_returns200WithSuccessEnvelope() throws Exception {
        when(roomService.delete(1L, 42L)).thenReturn(1L);

        mvc.perform(delete("/api/v1/rooms/{roomId}", 1L)
                        .with(auth())
                        .with(csrf())
                        .param("requesterId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void delete_forbidden_returns403() throws Exception {
        doThrow(new CustomException(ErrorCode.ROOM_NOT_HOST)).when(roomService).delete(1L, 77L);

        mvc.perform(delete("/api/v1/rooms/{roomId}", 1L)
                        .with(auth())
                        .with(csrf())
                        .param("requesterId", "77"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new CustomException(ErrorCode.ROOM_NOT_FOUND)).when(roomService).delete(999L, 42L);

        mvc.perform(delete("/api/v1/rooms/{roomId}", 999L)
                        .with(auth())
                        .with(csrf())
                        .param("requesterId", "42"))
                .andExpect(status().isNotFound());
    }
}