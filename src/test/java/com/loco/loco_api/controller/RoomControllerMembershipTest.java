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
    void myRooms_ok() throws Exception {
        var r1 = new RoomResponse(10L, "A","d", false, null, 1L, "X");
        var r2 = new RoomResponse(20L, "B","d", true, null, 99L, "Y");
        when(roomService.listMy(1L)).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/v1/rooms/my")
                        .with(user("tester").roles("USER"))
                        .param("userId","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(10))
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
