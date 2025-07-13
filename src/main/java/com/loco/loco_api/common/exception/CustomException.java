package com.loco.loco_api.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 사용할 커스텀 예외 클래스
 *
 * - 단순한 런타임 예외가 아닌, ErrorCode를 기반으로 한 명확한 예외 구조를 제공
 * - 서비스 계층에서 발생하는 예외 상황을 통일된 형식으로 처리할 수 있도록 함
 * - GlobalExceptionHandler에서 이 예외를 처리하여 일관된 API 응답을 반환
 *
 * 사용 예시:
 *   throw new CustomException(ErrorCode.USER_NOT_FOUND);
 *
 * 처리 흐름:
 *   1. 서비스/도메인 로직에서 CustomException 발생
 *   2. GlobalExceptionHandler에서 @ExceptionHandler(CustomException.class)로 처리
 *   3. ErrorCode의 httpStatus + message + code를 기반으로 응답 전송
 *
 * 주의사항:
 * - 반드시 ErrorCode enum을 통해 생성해야 하며, 임의 문자열 사용 금지
 * - 사용자에게 노출되는 메시지는 ErrorCode에서 정의된 값만 사용
 */
@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  /**
   * ErrorCode 기반 커스텀 예외 생성자
   *
   * @param errorCode 비즈니스 로직에 정의된 에러 코드
   */
  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());  // RuntimeException의 message 필드에 세팅
    this.errorCode = errorCode;
  }
}
