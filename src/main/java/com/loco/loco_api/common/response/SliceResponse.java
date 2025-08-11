package com.loco.loco_api.common.response;

import java.util.List;

/**
 * 공통 인피니티 스크롤(무한 스크롤) 응답 DTO
 *
 * Spring Data JPA의 Slice<T>를 기반으로,
 * 무한 스크롤 UI에 필요한 최소한의 정보만 제공하는 커스텀 응답 클래스
 *
 *   총 데이터 개수(totalElements)나 전체 페이지 수(totalPages)를 내려주지 않음 → 성능 최적화
 *   현재 페이지의 데이터 목록과 다음 페이지 존재 여부만 포함
 *   대량 데이터 환경에서 커서 기반 페이지네이션으로 확장 가능
 *
 *
 * @param <T> 실제 데이터 타입 (예: NoticeGetRequest)
 */
public record SliceResponse<T>(
        List<T> content,  // 현재 페이지(슬라이스)의 데이터 목록
        boolean hasNext   // 다음 페이지(슬라이스) 존재 여부
) {
  /**
   * SliceResponse 생성 헬퍼 메서드
   *
   * @param content 현재 페이지(슬라이스)의 데이터 목록
   * @param hasNext 다음 페이지 존재 여부
   */
  public static <T> SliceResponse<T> of(List<T> content, boolean hasNext) {
    return new SliceResponse<>(content, hasNext);
  }
}
