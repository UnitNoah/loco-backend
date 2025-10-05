package com.loco.loco_api.controller;

import com.loco.loco_api.common.dto.user.request.UserUpdateRequest;
import com.loco.loco_api.common.dto.user.response.UserDeleteResponse;
import com.loco.loco_api.common.dto.user.response.UserResponse;
import com.loco.loco_api.common.response.ApiResponse;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/profile")
  public ApiResponse<?> getProfile(@AuthenticationPrincipal Jwt jwt) {

    if (jwt == null) {
      // 로그인되지 않은 사용자도 SUCCESS 형식으로 응답 (data = Map)
      return ApiResponse.success(Map.of(
              "status", "guest",
              "message", "로그인되지 않은 사용자입니다."
      ));
    }


    // 서비스 계층에서 현재 로그인한 유저 조회
    UserEntity user = userService.getCurrentUser(jwt);

    UserResponse response = new UserResponse(
            user.getId().toString(),
            user.getNickname(),
            user.getEmail(),
            user.getProfileImageUrl(),
            List.of("ROLE_USER") // 추후 UserEntity에 role 컬럼을 추가해서 매핑
    );

    return ApiResponse.success(response);
  }

  @PutMapping
  public ApiResponse<UserResponse> updateUser(
          @AuthenticationPrincipal Jwt jwt,
          @RequestBody UserUpdateRequest request
  ) {
    UserEntity updatedUser = userService.updateUser(jwt, request);

    UserResponse response = new UserResponse(
            updatedUser.getId().toString(),
            updatedUser.getNickname(),
            updatedUser.getEmail(),
            updatedUser.getProfileImageUrl(),
            List.of("ROLE_USER")
    );

    return ApiResponse.success(response);
  }

  @DeleteMapping
  public ApiResponse<UserDeleteResponse> deleteUser(@AuthenticationPrincipal Jwt jwt) {
    userService.deleteUser(jwt);

    return ApiResponse.success(new UserDeleteResponse("회원 탈퇴가 완료되었습니다."));
  }

  @PostMapping("/logout")
  public ApiResponse<String> logout(HttpServletResponse response, HttpServletRequest request) {
    boolean secure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    // access_token 쿠키 삭제
    ResponseCookie cookie = ResponseCookie.from("access_token", "")
            .path("/")
            .maxAge(0)
            .httpOnly(true)
            .secure(secure)             // HTTPS일 경우 필수
            .sameSite(secure ? "None" : "Lax") // 로컬(http)은 Lax, 운영(https)은 None
            .build();
    response.addHeader("Set-Cookie", cookie.toString());
    return ApiResponse.success("로그아웃이 완료되었습니다.");
  }
}
