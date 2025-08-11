package com.loco.loco_api.common.response;

import com.loco.loco_api.common.exception.ErrorCode;

/**
 * API 응답 포맷 표준화 record
 *
 *   모든 컨트롤러 응답을 통일된 JSON 구조로 반환하기 위한 DTO
 *
 *   불변 객체 → record 특성상 모든 필드가 final
 *   성공/실패 응답 생성 헬퍼 메서드 제공
 *   코드와 메시지를 통해 상태 식별 및 프론트 분기 처리 가능
 *
 * @param <T> 응답 데이터 타입
 */
public record ApiResponse<T>(
        String code,     // 에러 또는 성공 구분 코드 (예: SUCCESS, USER_NOT_FOUND)
        String message,  // 사용자 메시지
        T data           // 응답 데이터 (성공 시 payload, 실패 시 null)
) {
  /**
   * 성공 응답 생성
   *
   * @param data 응답에 포함할 본문 데이터
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
  }

  /**
   * 실패 응답 생성 (ErrorCode 기반)
   *
   * @param errorCode 사전에 정의된 비즈니스 에러 코드
   */
  public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
    return new ApiResponse<>(errorCode.name(), errorCode.getMessage(), null);
  }
}
