package com.loco.loco_api.common.exception;

import com.loco.loco_api.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 클래스
 *
 * 컨트롤러 전반에서 발생하는 예외를 한 곳에서 처리
 * CustomException이 발생할 경우 HTTP 상태 코드와 함께
 * 일관된 응답 형식(ApiResponse)을 반환
 *
 * - @RestControllerAdvice는 모든 @RestController에 적용
 * - 서비스 레이어에서 throw new CustomException(...) 호출 시 이곳에서 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 커스텀 비즈니스 예외 처리 핸들러
   *
   * @param ex 서비스 로직에서 발생한 CustomException
   * @return ApiResponse 형식의 응답 본문과 HTTP 상태 코드
   *
   * ex) 사용자가 존재하지 않음, 권한 없음 등의 비즈니스 예외
   *
   * {
   *   "code": "USER_NOT_FOUND",
   *   "message": "사용자를 찾을 수 없습니다.",
   *   "data": null
   * }
   */
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException ex) {
    var errorCode = ex.getErrorCode();
    return ResponseEntity
            .status(errorCode.getHttpStatus())     // ex: 404, 403
            .body(ApiResponse.fail(errorCode));    // ApiResponse 형태로 통일된 응답
  }
}
