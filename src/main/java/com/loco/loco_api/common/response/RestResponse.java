package com.loco.loco_api.common.response;

import com.loco.loco_api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 컨트롤러에서 공통 응답을 간결하게 리턴할 수 있도록 도와주는 응답 헬퍼 클래스입니다.
 *
 * 팀원들이 ResponseEntity, ApiResponse를 직접 조합하지 않고도,
 * 단일 메서드 호출만으로 일관된 응답 구조를 사용할 수 있게 해줍니다.
 *
 * 사용 예시:
 *   return RestResponse.ok(data);
 *   return RestResponse.created(data);
 *   return RestResponse.fail(ErrorCode.USER_NOT_FOUND);
 */
public class RestResponse {

  /**
   * HTTP 200 OK 응답을 생성
   *
   * @param data 실제 응답 데이터
   * @param <T> 응답 데이터의 타입
   * @return ApiResponse<T>를 감싼 ResponseEntity
   */
  public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
    return ResponseEntity.ok(ApiResponse.success(data));
  }

  /**
   * HTTP 201 Created 응답을 생성
   *
   * @param data 생성된 리소스 데이터
   * @param <T> 응답 데이터의 타입
   * @return ApiResponse<T>를 감싼 ResponseEntity
   */
  public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
  }

  /**
   * HTTP 상태코드를 ErrorCode 기반으로 결정하여 실패 응답을 생성
   *
   * @param errorCode 사전에 정의된 비즈니스 에러 코드
   * @return ApiResponse<Void>를 감싼 ResponseEntity
   */
  public static ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode) {
    return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.fail(errorCode));
  }
}
