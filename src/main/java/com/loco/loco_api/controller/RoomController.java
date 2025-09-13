package com.loco.loco_api.controller;

import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.response.ApiResponse;
import com.loco.loco_api.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room", description = "방 API")
@SecurityRequirement(name = "JWT")
public class RoomController {
    private final RoomService service;

    @PostMapping
    @Operation(summary = "방 생성", description = "새로운 방을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    public ApiResponse<RoomResponse> createRoom(
            @RequestParam Long hostId,
            @Valid @RequestBody RoomCreateRequest req
    ){
        return ApiResponse.success(service.create(req, hostId));
    }
}
