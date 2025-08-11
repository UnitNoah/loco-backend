package com.loco.loco_api.common.dto.notice.response;

import com.loco.loco_api.domain.notice.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

// 조회용 (리스트/상세 공용)
@Schema(description = "공지 단건 응답")
public record NoticeResponse(
        @Schema(description = "공지 ID", example = "123") Long id,
        @Schema(description = "제목", example = "점검 안내") String title,
        @Schema(description = "내용", example = "8/15 02:00~03:00 점검") String content
)
{
  public static NoticeResponse from(Notice n) {
    return new NoticeResponse(n.getId(), n.getTitle(), n.getContent());
  }
}