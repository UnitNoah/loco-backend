package com.loco.loco_api.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 공통 페이지네이션 응답 DTO
 *
 *   Spring Data JPA의 Page<T>를 그대로 반환하지 않고,
 *   API 응답 규격에 맞춘 커스텀 페이지 래퍼 클래스
 *
 *   JPA 내부 구조(Pageable, Sort 등)를 직접 노출하지 않음
 *   프론트엔드에서 사용하기 쉽도록 필요한 메타데이터만 제공
 *   API 전반에서 통일된 페이지네이션 응답 형식을 유지
 *
 *
 * @param <T> 실제 데이터 타입 (예: NoticeGetRequest)
 */
@Schema(description = "페이지네이션 응답 DTO")
public record PageResponse<T>(

        @Schema(description = "현재 페이지의 데이터 목록")
        List<T> content,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 당 데이터 개수", example = "20")
        int size,

        @Schema(description = "전체 데이터 개수", example = "152")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "8")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious,

        @Schema(description = "정렬 정보 (필요 없으면 null)")
        List<SortOrder> sort
) {

  /**
   * PageResponse 생성 헬퍼 메서드
   *
   * 정렬 정보가 필요 없을 경우 이 메서드를 사용
   */
  public static <T> PageResponse<T> of(
          List<T> content,
          int page,
          int size,
          long totalElements,
          int totalPages,
          boolean hasNext,
          boolean hasPrevious
  ) {
    return new PageResponse<>(content, page, size, totalElements, totalPages, hasNext, hasPrevious, null);
  }

  /**
   * 정렬 정보 표현용 내부 DTO
   *
   * @param property 정렬 기준 필드명
   * @param direction 정렬 방향 ("asc" or "desc")
   */
  @Schema(description = "정렬 정보")
  public record SortOrder(
          @Schema(description = "정렬 기준 필드명", example = "id")
          String property,

          @Schema(description = "정렬 방향", example = "desc")
          String direction
  ) {}
}
