package com.loco.loco_api.common.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 수정 요청 DTO")
public record UserUpdateRequest(
        @Schema(description = "닉네임", example = "dev_ian")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/img.png")
        String profileImageUrl
) {}
