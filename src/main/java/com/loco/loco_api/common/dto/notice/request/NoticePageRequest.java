package com.loco.loco_api.common.dto.notice.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지 페이지 요청 DTO")
public record NoticePageRequest(

        @Schema(description = "조회할 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 당 데이터 개수", example = "20")
        int size,

        @Schema(description = "정렬 기준 필드명", example = "id")
        String sortBy,

        @Schema(description = "정렬 방향 (asc 또는 desc)", allowableValues = {"asc", "desc"}, example = "desc")
        String sortDirection
) {}
