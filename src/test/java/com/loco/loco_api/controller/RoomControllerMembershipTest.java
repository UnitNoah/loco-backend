package com.loco.loco_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RoomController.class)
public class RoomControllerMembershipTest {
    @MockBean(name = "jpaMappingContext")
    JpaMetamodelMappingContext jpaMappingContext;

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    RoomService roomService;

    @Test
    void hostedRooms_ok() throws Exception {
        var room1 = new RoomResponse(10L, "A", "d", false, null, 1L, "X");
        var room2 = new RoomResponse(11L, "B", "d", true, null, 1L, "Y");
        when(roomService.listHosted(1L)).thenReturn(List.of(room2, room1));

        mvc.perform(get("/api/v1/rooms/hosted").with(user("tester").roles("USER")).param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(11))
                .andExpect(jsonPath("$.data[1].id").value(10));
    }

    @Test
    void joinedRooms_ok() throws Exception {
        var room1 = new RoomResponse(20L, "J1", "d", false, null, 99L, "X");
        var room2 = new RoomResponse(30L, "32", "d", true, null, 77L, "Y");
        when(roomService.listJoined(1L)).thenReturn(List.of(room2, room1));

        mvc.perform(get("/api/v1/rooms/joined").with(user("tester").roles("USER")).param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(30))
                .andExpect(jsonPath("$.data[1].id").value(20));
    }

    @Test
    void join_public_ok() throws Exception {
        mvc.perform(post("/api/v1/rooms/{roomId}/join", 100L)
                        .with(user("tester").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void join_private_wrongCode_400() throws Exception {
        doThrow(new CustomException(ErrorCode.ROOM_INVALID_INVITE_CODE))
                .when(roomService).join(eq(100L), eq(1L), eq("BAD"));

        mvc.perform(post("/api/v1/rooms/{roomId}/join", 100L)
                        .with(user("tester").roles("USER"))
                        .with(csrf())
                        .param("userId","1")
                        .param("inviteCode","BAD"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void leave_noContent() throws Exception {
        mvc.perform(post("/api/v1/rooms/{roomId}/leave", 100L)
                        .with(user("tester").roles("USER"))
                        .with(csrf())
                        .param("userId","1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void leave_hostForbidden_403() throws Exception {
        doThrow(new CustomException(ErrorCode.ROOM_HOST_CANNOT_LEAVE))
                .when(roomService).leave(100L, 1L);

        mvc.perform(post("/api/v1/rooms/{roomId}/leave", 100L)
                        .with(user("tester").roles("USER"))
                        .with(csrf())
                        .param("userId","1"))
                .andExpect(status().isForbidden());
    }
}
