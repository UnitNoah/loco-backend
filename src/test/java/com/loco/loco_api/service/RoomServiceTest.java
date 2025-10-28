package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.request.RoomUpdateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.RoomParticipantRepository;
import com.loco.loco_api.repository.RoomRepository;
import com.loco.loco_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private RoomRepository rooms;
    @Mock private UserRepository users;
    @Mock private RoomParticipantRepository participants; // <-- needed by RoomService ctor

    @InjectMocks private RoomService roomService;

    // --- 상세 조회 (분리된 엔드포인트 대응) ---

    @Test
    void getPublicDetail_success_whenRoomIsPublic() {
        var host = UserEntity.builder()
                .id(42L).nickname("홍길동").provider("google").oauthId("x")
                .build();

        var room = Room.builder()
                .id(1L)
                .name("스터디룸")
                .description("조용한 곳")
                .isPrivate(false)
                .thumbnail("https://cdn.example.com/img.png")
                .inviteCode("ABCD1234")
                .host(host)
                .build();

        when(rooms.findActiveByIdFetchHost(1L)).thenReturn(Optional.of(room));

        RoomResponse resp = roomService.getPublicDetail(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.name()).isEqualTo("스터디룸");
        assertThat(resp.description()).isEqualTo("조용한 곳");
        assertThat(resp.isPrivate()).isFalse();
        assertThat(resp.thumbnail()).isEqualTo("https://cdn.example.com/img.png");
        assertThat(resp.hostId()).isEqualTo(42L);
        assertThat(resp.inviteCode()).isEqualTo("ABCD1234");
    }

    @Test
    void getPublicDetail_404_whenRoomIsPrivate() {
        var host = UserEntity.builder().id(1L).nickname("h").provider("g").oauthId("x").build();
        var room = Room.builder().id(10L).name("비공개").description("d").isPrivate(true).inviteCode("X").host(host).build();
        when(rooms.findActiveByIdFetchHost(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.getPublicDetail(10L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void getPrivateDetail_success_whenRoomIsPrivate() {
        var host = UserEntity.builder()
                .id(42L).nickname("홍길동").provider("google").oauthId("x")
                .build();

        var room = Room.builder()
                .id(2L)
                .name("비공개 스터디룸")
                .description("d")
                .isPrivate(true)
                .thumbnail("t.png")
                .inviteCode("INV123")
                .host(host)
                .build();

        when(rooms.findActiveByIdFetchHost(2L)).thenReturn(Optional.of(room));

        RoomResponse resp = roomService.getPrivateDetail(2L);

        assertThat(resp.id()).isEqualTo(2L);
        assertThat(resp.isPrivate()).isTrue();
        assertThat(resp.hostId()).isEqualTo(42L);
    }

    @Test
    void getPrivateDetail_404_whenRoomIsPublic() {
        var host = UserEntity.builder().id(1L).nickname("h").provider("g").oauthId("x").build();
        var room = Room.builder().id(20L).name("공개").description("d").isPrivate(false).inviteCode("C").host(host).build();
        when(rooms.findActiveByIdFetchHost(20L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.getPrivateDetail(20L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void getPublicDetail_notFound() {
        when(rooms.findActiveByIdFetchHost(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getPublicDetail(999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void getPrivateDetail_notFound() {
        when(rooms.findActiveByIdFetchHost(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getPrivateDetail(999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    // --- 공개/비공개 목록 ---

    @Test
    void listPublic_filtersAndMaps_inCreatedAtDescOrder() {
        var host = UserEntity.builder().id(42L).nickname("홍길동").provider("google").oauthId("x").build();

        var r3 = Room.builder().id(3L).name("공개3").description("d").isPrivate(false).inviteCode("C3").thumbnail("t").host(host).build();
        var r2 = Room.builder().id(2L).name("공개2").description("d").isPrivate(false).inviteCode("C2").thumbnail("t").host(host).build();
        var r1 = Room.builder().id(1L).name("공개1").description("d").isPrivate(false).inviteCode("C1").thumbnail("t").host(host).build();

        when(rooms.findPublicOrderByCreatedAtDesc()).thenReturn(List.of(r3, r2, r1));

        List<RoomResponse> list = roomService.listPublic();

        assertThat(list).hasSize(3);
        assertThat(list).extracting(RoomResponse::id).containsExactly(3L, 2L, 1L);
        assertThat(list).allMatch(rr -> !rr.isPrivate());

        verify(rooms, times(1)).findPublicOrderByCreatedAtDesc();
        verifyNoMoreInteractions(rooms);
    }

    @Test
    void listPrivate_filtersAndMaps_inCreatedAtDescOrder() {
        var host = UserEntity.builder().id(7L).nickname("이몽룡").provider("google").oauthId("y").build();

        var r5 = Room.builder().id(5L).name("비공개5").description("d").isPrivate(true).inviteCode("X5").thumbnail("t").host(host).build();
        var r4 = Room.builder().id(4L).name("비공개4").description("d").isPrivate(true).inviteCode("X4").thumbnail("t").host(host).build();

        when(rooms.findPrivateOrderByCreatedAtDesc()).thenReturn(List.of(r5, r4));

        List<RoomResponse> list = roomService.listPrivate();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(RoomResponse::id).containsExactly(5L, 4L);
        assertThat(list).allMatch(RoomResponse::isPrivate);

        verify(rooms, times(1)).findPrivateOrderByCreatedAtDesc();
        verifyNoMoreInteractions(rooms);
    }

    @Test
    void listPublic_empty_returnsEmptyList() {
        when(rooms.findPublicOrderByCreatedAtDesc()).thenReturn(List.of());

        List<RoomResponse> list = roomService.listPublic();

        assertThat(list).isEmpty();
        verify(rooms).findPublicOrderByCreatedAtDesc();
    }

    // --- 생성/수정/삭제 ---

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

    private Room roomWithHost(long roomId, long hostId) {
        var host = UserEntity.builder().id(hostId).nickname("host").provider("google").oauthId("x").build();

        return Room.builder()
                .id(roomId)
                .name("old")
                .description("old desc")
                .isPrivate(true)
                .inviteCode("CODE")
                .thumbnail("old.png")
                .host(host)
                .build();
    }

    @Test
    void update_success_hostOnly() {
        var room = roomWithHost(1L, 42L);
        when(rooms.findActiveByIdFetchHost(1L)).thenReturn(Optional.of(room));
        when(rooms.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new RoomUpdateRequest("new", "new desc", false, "new.png");
        RoomResponse resp = roomService.update(1L, 42L, req);

        assertThat(resp.name()).isEqualTo("new");
        assertThat(resp.description()).isEqualTo("new desc");
        assertThat(resp.isPrivate()).isFalse();
        assertThat(resp.thumbnail()).isEqualTo("new.png");
    }

    @Test
    void update_forbidden_whenNotHost() {
        var room = roomWithHost(1L, 42L);
        when(rooms.findActiveByIdFetchHost(1L)).thenReturn(Optional.of(room));

        var req = new RoomUpdateRequest("new", null, null, null);

        assertThatThrownBy(() -> roomService.update(1L, 77L, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_HOST);

        verify(rooms, never()).save(any());
    }

    @Test
    void update_notFound() {
        when(rooms.findActiveByIdFetchHost(999L)).thenReturn(Optional.empty());
        var req = new RoomUpdateRequest("x", null, null, null);

        assertThatThrownBy(() -> roomService.update(999L, 42L, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void delete_success_hostOnly() {
        var room = roomWithHost(1L, 42L);
        when(rooms.findActiveByIdFetchHost(1L)).thenReturn(Optional.of(room));

        roomService.delete(1L, 42L);

        verify(rooms).delete(room);
    }

    @Test
    void delete_forbidden_whenNotHost() {
        var room = roomWithHost(1L, 42L);
        when(rooms.findActiveByIdFetchHost(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.delete(1L, 77L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_HOST);

        verify(rooms, never()).delete(any());
    }

    @Test
    void delete_notFound() {
        when(rooms.findActiveByIdFetchHost(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.delete(999L, 42L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);

        verify(rooms, never()).delete(any());
    }
}
