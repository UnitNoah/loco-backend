package com.loco.loco_api.controller;

import com.loco.loco_api.common.dto.user.response.UserResponse;
import com.loco.loco_api.common.response.ApiResponse;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/profile")
  public ApiResponse<UserResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
    // 서비스 계층에서 현재 로그인한 유저 조회
    UserEntity user = userService.getCurrentUser(jwt);

    UserResponse response = new UserResponse(
            user.getId().toString(),
            user.getNickname(),
            user.getProfileImageUrl(),
            List.of("ROLE_USER") // 추후 UserEntity에 role 컬럼을 추가해서 매핑
    );

    return ApiResponse.success(response);
  }
}
