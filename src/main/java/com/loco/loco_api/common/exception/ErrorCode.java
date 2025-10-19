package com.loco.loco_api.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 시스템 전반에서 사용하는 공통 에러 코드 정의 Enum
 *
 * - 각 에러 항목은 HTTP 상태 코드, 에러 코드 문자열, 사용자 메시지를 포함합니다.
 * - 비즈니스 로직에서 CustomException과 함께 사용되며,
 *   GlobalExceptionHandler를 통해 일관된 응답 포맷(ApiResponse)으로 변환됩니다.
 *
 * 사용 예시:
 *   throw new CustomException(ErrorCode.USER_NOT_FOUND);
 *
 * 응답 예시 (JSON):
 * {
 *   "code": "USER_NOT_FOUND",
 *   "message": "사용자를 찾을 수 없습니다.",
 *   "data": null
 * }
 *
 * ✅ 에러 항목 추가 시 주의사항:
 * - HTTP 상태코드와 에러코드(code)는 의미에 맞게 설정할 것
 * - code는 중복되지 않도록 관리할 것 (프론트에서 분기처리용)
 * - 사용자 메시지는 사용자 친화적인 표현으로 작성할 것
 */

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 인증/인가
  AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4001, "인증이 필요한 요청입니다."),
  AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, 4003, "요청에 대한 권한이 없습니다."),
  AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 4001, "유효하지 않은 액세스 토큰입니다."),
  AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 4001, "만료된 토큰입니다."),
  AUTH_NO_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 4001, "리프레시 토큰이 존재하지 않습니다."),

  // 사용자
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "사용자 정보를 찾을 수 없습니다."),

  // 방
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "해당 방을 찾을 수 없습니다."),
  ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, 4003, "방에 접근할 수 없습니다."),
  ROOM_IS_PRIVATE(HttpStatus.BAD_REQUEST, 4000, "비공개 방은 링크를 통해서만 접근할 수 있습니다."),
  ROOM_NOT_HOST(HttpStatus.FORBIDDEN, 4003, "해당 작업은 작성자 또는 방장만 수행할 수 있습니다."),
  ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "방 참여자 정보를 찾을 수 없습니다."),
  ROOM_ALREADY_JOINED(HttpStatus.FORBIDDEN, 4003, "방에 이미 참가중입니다."),
  ROOM_INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, 4003, "유효하지 않은 참여 코드입니다."),
  ROOM_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "방에 참여하지 않은 사용자입니다."),
  ROOM_HOST_CANNOT_LEAVE(HttpStatus.FORBIDDEN, 4003, "호스트는 방에서 나갈 수 없습니다."),
  // 위치
  LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "위치 정보를 찾을 수 없습니다."),
  LOCATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, 4003, "해당 위치에 대한 권한이 없습니다."),

  // 장소
  PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "장소를 찾을 수 없습니다."),
  PLACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, 4003, "장소 접근 권한이 없습니다."),
  PLACE_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 4004, "장소 이미지 업로드에 실패했습니다."),

  // 알림
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "알림을 찾을 수 없습니다."),

  // 유효성
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, 4000, "잘못된 입력값입니다."),
  MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, 4000, "필수 입력 항목이 누락되었습니다."),
  INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, 4000, "요청 파라미터 형식이 올바르지 않습니다."),

  // 공지사항(Notice)
  NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "공지를 찾을 수 없습니다."),

  // 서버 오류
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 내부 오류입니다."),
  DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "데이터베이스 오류가 발생했습니다."),
  EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "외부 API 호출 중 오류가 발생했습니다."),
  USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, 5000, "이미 탈퇴한 회원입니다."),;

  private final HttpStatus httpStatus;
  private final int code;           // 시스템 내부 관리용 숫자 코드
  private final String message;     // 사용자에게 보여줄 메시지
}
