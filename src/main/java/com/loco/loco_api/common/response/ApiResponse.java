package com.loco.loco_api.common.response;

import com.loco.loco_api.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 응답 포맷 표준화 클래스
 *
 * - 모든 컨트롤러 응답은 이 클래스를 통해 통일된 JSON 구조로 반환
 * - 성공/실패 응답에 대해 각각의 정적 팩토리 메서드(success, fail)를 제공
 *
 * 응답 구조 예시 (성공):
 * {
 *   "code": "SUCCESS",
 *   "message": "요청이 성공적으로 처리되었습니다.",
 *   "data": {
 *     "id": 1,
 *     "username": "locoUser"
 *   }
 * }
 *
 * 응답 구조 예시 (실패):
 * {
 *   "code": "USER_NOT_FOUND",
 *   "message": "유저를 찾을 수 없습니다.",
 *   "data": null
 * }
 *
 * 사용 방법:
 *   return ApiResponse.success(data);
 *   return ApiResponse.fail(ErrorCode.USER_NOT_FOUND);
 *
 * 주의사항:
 * - 실패 응답은 반드시 ErrorCode 기반으로 생성할 것
 * - code는 내부 에러 분석 및 프론트 분기처리에 사용됨
 *
 * @param <T> data 필드의 응답 데이터 타입 (nullable)
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

  /** 에러 또는 성공 구분용 코드 문자열 (예: SUCCESS, USER_NOT_FOUND) */
  private final String code;

  /** 사용자에게 보여줄 메시지 */
  private final String message;

  /** 응답 데이터 (성공 시 payload, 실패 시 null) */
  private final T data;

  /**
   * 성공 응답 생성 (HTTP 200)
   *
   * @param data 응답에 포함될 본문 데이터
   * @param <T> 데이터 타입
   * @return ApiResponse 인스턴스
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
  }

  /**
   * 실패 응답 생성 (ErrorCode 기반)
   *
   * @param errorCode 사전에 정의된 비즈니스 에러 코드
   * @param <T> 응답 본문의 타입 (보통 null)
   * @return ApiResponse 인스턴스
   */
  public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
    return new ApiResponse<>(errorCode.name(), errorCode.getMessage(), null);
  }
}
