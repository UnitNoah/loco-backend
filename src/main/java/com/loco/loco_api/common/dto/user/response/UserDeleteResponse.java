package com.loco.loco_api.common.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 응답 DTO")
public record UserDeleteResponse(
        @Schema(description = "탈퇴 완료 메시지", example = "회원 탈퇴가 완료되었습니다.")
        String message
) {}

