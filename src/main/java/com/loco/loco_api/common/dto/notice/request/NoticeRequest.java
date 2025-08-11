package com.loco.loco_api.common.dto.notice.request;

import com.loco.loco_api.domain.notice.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지 조회 응답 DTO")
public record NoticeRequest(

        @Schema(description = "공지 제목", example = "서버 점검 안내")
        String title,

        @Schema(description = "공지 내용", example = "8월 15일 02:00~03:00 서버 점검이 예정되어 있습니다.")
        String content
) {
  public static NoticeRequest from(Notice notice) {
    return new NoticeRequest(notice.getTitle(), notice.getContent());
  }
}
