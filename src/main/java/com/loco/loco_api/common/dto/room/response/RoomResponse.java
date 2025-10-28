package com.loco.loco_api.common.dto.room.response;

import com.loco.loco_api.domain.room.Room;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "방 생성/조회 응답")
public record RoomResponse(
    @Schema(description = "방 ID", example = "123") Long id,
    @Schema(description = "방 이름", example = "카공하기 좋은 카페") String name,
    @Schema(description = "설명", example = "나만 알기 아까운") String description,
    @Schema(description = "비공개 여부", example = "true") Boolean isPrivate,
    @Schema(description = "썸네일 URL", example = "https://cdn.example.com/rooms/abc.png") String thumbnail,
    @Schema(description = "호스트 사용자 ID", example = "1") Long hostId,
    @Schema(description = "초대 코드", example = "aaaaa") String inviteCode,
    @Schema(description = "호스트 닉네임", example = "jin") String hostNickname,
    @Schema(description = "호스트 프로필 이미지 URL", example = "https://cdn.example.com/users/u1.png") String hostProfileImageUrl
    ){
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.isPrivate(),
                room.getThumbnail(),
                room.getHost().getId(),
                room.getInviteCode(),
                room.getHost().getNickname(),
                room.getHost().getProfileImageUrl()
        );
    }
}
