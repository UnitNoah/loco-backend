package com.loco.loco_api.common.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 프로필 응답 DTO")
public record UserResponse(

        @Schema(description = "유저 고유 식별자 (sub)", example = "google_1234567890")
        String userId,

        @Schema(description = "닉네임 / 표시 이름", example = "이안")
        String name,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.loco.com/profile/abc.jpg")
        String profileImage,

        @Schema(description = "권한 목록", example = "[\"ROLE_USER\"]")
        java.util.List<String> roles
) {}

