package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.RoomRepository;
import com.loco.loco_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private RoomRepository rooms;
    @Mock private UserRepository users;

    @InjectMocks private RoomService roomService;

    // 방 상세 조회
    @Test
    void getDetail_success() {
        var host = UserEntity.builder()
                .id(42L).nickname("홍길동").provider("google").oauthId("x")
                .build();

        var room = Room.builder()
                .id(1L)
                .name("스터디룸")
                .description("조용한 곳")
                .isPrivate(true)
                .thumbnail("https://cdn.example.com/img.png")
                .inviteCode("ABCD1234")
                .host(host)
                .build();

        when(rooms.findById(1L)).thenReturn(Optional.of(room));


        RoomResponse resp = roomService.getDetail(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.name()).isEqualTo("스터디룸");
        assertThat(resp.description()).isEqualTo("조용한 곳");
        assertThat(resp.isPrivate()).isTrue();
        assertThat(resp.thumbnail()).isEqualTo("https://cdn.example.com/img.png");
        assertThat(resp.hostId()).isEqualTo(42L);
        assertThat(resp.inviteCode()).isEqualTo("ABCD1234");
    }

    @Test
    void getDetail_notFound() {
        when(rooms.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getDetail(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 방을 찾을 수 없습니다.");
    }


    // 방 생성
    @Test
    void create_success() {
        UserEntity host = UserEntity.builder()
                .id(42L).nickname("홍길동").provider("google").oauthId("123456")
                .build();
        when(users.findById(42L)).thenReturn(Optional.of(host));
        when(rooms.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RoomCreateRequest req = new RoomCreateRequest("스터디룸", "조용", true, "https://cdn.example.com/img.png");
        RoomResponse resp = roomService.create(req, 42L);

        assertThat(resp.name()).isEqualTo("스터디룸");
        verify(rooms).save(any());
    }

    @Test
    void create_fail_whenHostMissing() {
        when(users.findById(9999L)).thenReturn(Optional.empty());
        RoomCreateRequest req = new RoomCreateRequest("스터디룸", "조용", true, null);

        assertThatThrownBy(() -> roomService.create(req, 9999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Host user not found");
    }
}/**/