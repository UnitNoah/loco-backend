package com.loco.loco_api.controller;

import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.request.RoomUpdateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.response.ApiResponse;
import com.loco.loco_api.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room", description = "방 API")
@SecurityRequirement(name = "JWT")
public class RoomController {
    private final RoomService service;

    @GetMapping("/{roomId}")
    @Operation(summary = "방 상세 조회", description = "단건 방 상세를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방 없음")
    public ApiResponse<RoomResponse> getRoom(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId
    ) {
        return ApiResponse.success(service.getDetail(roomId));
    }

    @GetMapping("/public")
    @Operation(summary = "공개방 목록 조회", description = "공개방을 최신순으로 반환합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    public ApiResponse<List<RoomResponse>> listPublicRooms() {
        return ApiResponse.success(service.listPublic());
    }

    @GetMapping("/private")
    @Operation(summary = "비공개방 목록 조회", description = "비공개방을 최신순으로 반환합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    public ApiResponse<List<RoomResponse>> listPrivateRooms() {
        return ApiResponse.success(service.listPrivate());
    }

    @PostMapping
    @Operation(summary = "방 생성", description = "새로운 방을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    public ApiResponse<RoomResponse> createRoom(
            @RequestParam Long hostId,
            @Valid @RequestBody RoomCreateRequest req
    ){
        return ApiResponse.success(service.create(req, hostId));
    }

    @PatchMapping("/{roomId}")
    @Operation(summary = "방 정보 수정", description = "방 정보를 부분 수정합니다.")
    public ApiResponse<RoomResponse> updateRoom(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "요청자 ID", example = "42") @RequestParam Long requesterId,
            @Valid @RequestBody RoomUpdateRequest req
    ) {
        return ApiResponse.success(service.update(roomId, requesterId, req));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "방 삭제", description = "해당 방을 삭제합니다.")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "요청자 ID", example = "42") @RequestParam Long requesterId
    ) {
        service.delete(roomId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
