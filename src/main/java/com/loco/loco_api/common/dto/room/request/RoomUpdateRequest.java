package com.loco.loco_api.common.dto.room.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "방 수정 요청")
public record RoomUpdateRequest(
    @Schema(description = "방 이름", example = "카공하기 좋은 카페") @NotBlank
    String name,
    @Schema(description = "설명", example = "나만 알기 아까운") String description,
    @Schema(description = "비공개 여부", example = "true") Boolean isPrivate,
    @Schema(description = "썸네일 URL", example = "https://cdn.example.com/rooms/abc.png") String thumbnail
) {}
