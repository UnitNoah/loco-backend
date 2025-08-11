package com.loco.loco_api.common.dto.notice.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "공지 수정 요청")
public record NoticeUpdateRequest(
        @NotBlank @Size(max = 200)
        @Schema(description = "제목", example = "점검 안내(변경)") String title,
        @NotBlank
        @Schema(description = "내용", example = "점검 시간이 03:30까지 연장되었습니다.") String content
) {}