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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room", description = "방 API")
@SecurityRequirement(name = "JWT")
public class RoomController {
    private final RoomService service;

    @GetMapping("/public/{roomId}")
    @Operation(summary = "공개방 상세 조회", description = "단건 공개방 상세를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방 없음")
    public ApiResponse<RoomResponse> getPublicRoom(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId
    ) {
        return ApiResponse.success(service.getPublicDetail(roomId));
    }

    @GetMapping("/private/{roomId}")
    @Operation(summary = "비공개방 상세 조회", description = "단건 비공개방 상세를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방 없음")
    public ApiResponse<RoomResponse> getPrivateRoom(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId
    ) {
        return ApiResponse.success(service.getPrivateDetail(roomId));
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
    public ApiResponse<Long> deleteRoom(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "요청자 ID", example = "42") @RequestParam Long requesterId
    ) {
        service.delete(roomId, requesterId);
        return ApiResponse.success(service.delete(roomId, requesterId));
    }

    @GetMapping("/hosted")
    @Operation(summary = "내가 호스트인 방 목록", description = "요청자 ID가 호스트인 방들을 최신순으로 반환합니다.")
    public ApiResponse<List<RoomResponse>> hostedRooms(@RequestParam Long userId) {
        return ApiResponse.success(service.listHosted(userId));
    }

    @GetMapping("/joined")
    @Operation(summary = "내가 참여한 방 목록", description = "요청자 ID가 참여자로 속한 방들을 최신순으로 반환합니다.")
    public ApiResponse<List<RoomResponse>> joinedRooms(@RequestParam Long userId) {
        return ApiResponse.success(service.listJoined(userId));
    }

    @PostMapping("/{roomId}/join")
    @Operation(summary = "방 참여", description = "비공개방은 초대코드 필요.")
    public ApiResponse<RoomResponse> join(
            @PathVariable Long roomId,
            @RequestParam Long userId,
            @RequestParam(required = false) String inviteCode
    ) {
        service.join(roomId, userId, inviteCode);
        return ApiResponse.success(service.join(roomId, userId, inviteCode));
    }

    @PostMapping("/{roomId}/leave")
    @Operation(summary = "방 나가기", description = "호스트는 나갈 수 없음")
    public ApiResponse<RoomResponse> leave(
            @PathVariable Long roomId,
            @RequestParam Long userId
    ) {
        service.leave(roomId, userId);
        return ApiResponse.success(service.leave(roomId, userId));
    }
}
