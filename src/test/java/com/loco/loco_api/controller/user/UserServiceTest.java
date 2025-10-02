package com.loco.loco_api.controller.user;

import com.loco.loco_api.common.dto.user.request.UserUpdateRequest;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.UserRepository;
import com.loco.loco_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private Jwt jwt;
  private UserEntity activeUser;

  @BeforeEach
  void setUp() {
    // jwt mock
    jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn("google_12345");

    // 사용자 엔티티 (탈퇴하지 않은 상태)
    activeUser = UserEntity.builder()
            .id(1L)
            .nickname("oldNick")
            .profileImageUrl("http://img.old.png")
            .deletedAt(null)
            .build();
  }

  @Test
  void updateUser_success() {
    // given
    UserUpdateRequest request = new UserUpdateRequest("newNick", "http://img.new.png");
    when(userRepository.findByProviderAndOauthId(anyString(), anyString()))
            .thenReturn(Optional.of(activeUser));
    when(userRepository.save(any(UserEntity.class))).thenReturn(activeUser);

    // when
    UserEntity result = userService.updateUser(jwt, request);

    // then
    assertThat(result.getNickname()).isEqualTo("newNick");
    assertThat(result.getProfileImageUrl()).isEqualTo("http://img.new.png");
  }

  @Test
  void updateUser_throw_whenAlreadyDeleted() {
    // given
    activeUser.delete(); // deletedAt 세팅
    when(userRepository.findByProviderAndOauthId(anyString(), anyString()))
            .thenReturn(Optional.of(activeUser));

    // when & then
    assertThatThrownBy(() ->
            userService.updateUser(jwt, new UserUpdateRequest("nick", "http://img.png"))
    ).isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.USER_ALREADY_DELETED.getMessage());
  }

  @Test
  void updateUser_throw_whenInvalidNickname() {
    // given
    when(userRepository.findByProviderAndOauthId(anyString(), anyString()))
            .thenReturn(Optional.of(activeUser));

    // when & then
    assertThatThrownBy(() ->
            userService.updateUser(jwt, new UserUpdateRequest("   ", "http://img.png"))
    ).isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_INPUT_VALUE.getMessage());
  }

  @Test
  void deleteUser_success() {
    // given
    when(userRepository.findByProviderAndOauthId(anyString(), anyString()))
            .thenReturn(Optional.of(activeUser));

    // when
    userService.deleteUser(jwt);

    // then
    assertThat(activeUser.getDeletedAt()).isNotNull();
    verify(userRepository, times(1)).save(activeUser);
  }

  @Test
  void deleteUser_throw_whenAlreadyDeleted() {
    // given
    activeUser.delete();
    when(userRepository.findByProviderAndOauthId(anyString(), anyString()))
            .thenReturn(Optional.of(activeUser));

    // when & then
    assertThatThrownBy(() -> userService.deleteUser(jwt))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.USER_ALREADY_DELETED.getMessage());
  }
}
