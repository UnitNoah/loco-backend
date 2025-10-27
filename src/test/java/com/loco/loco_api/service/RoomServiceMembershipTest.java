package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.room.RoomParticipant;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.RoomParticipantRepository;
import com.loco.loco_api.repository.RoomRepository;
import com.loco.loco_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceMembershipTest {

    @Mock RoomRepository rooms;
    @Mock UserRepository users;
    @Mock RoomParticipantRepository participants;

    @InjectMocks RoomService service;

    private UserEntity user(long id) {
        return UserEntity.builder().id(id).provider("google").oauthId("x" + id).build();
    }

    private Room room(long id, long hostId, boolean priv) {
        return Room.builder()
                .id(id)
                .name("r" + id)
                .description("d")
                .isPrivate(priv)
                .inviteCode(priv ? "INV123" : null)
                .host(user(hostId))
                .build();
    }

    @Test
    void listHosted_returnsHostRooms_sortedByCreatedAtDesc() {
        long me = 1L;
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        Room room1 = room(10L, me, false);
        Room room2 = room(20L, me, true);

        room1.setCreatedAt(LocalDateTime.now().minusDays(1));
        room2.setCreatedAt(LocalDateTime.now());

        // Service uses: rooms.findHostBy(userId)
        when(rooms.findHostBy(me)).thenReturn(List.of(room2, room1));

        List<RoomResponse> out = service.listHosted(me);

        assertThat(out).extracting(RoomResponse::id).containsExactly(20L, 10L);
    }

    @Test
    void listJoined_returnsJoinedRooms_sortedByCreatedAtDesc_andSkipsSoftDeleted() {
        long me = 1L;
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        Room joinedNew = room(200L, 99L, false);
        Room joinedOld = room(100L, 88L, true);

        joinedNew.setCreatedAt(LocalDateTime.now());
        joinedOld.setCreatedAt(LocalDateTime.now().minusDays(2));

        // Service uses: participants.findJoinedRoomsBy(userId) -> List<Room>
        when(participants.findJoinedRoomsBy(me)).thenReturn(List.of(joinedNew, joinedOld));

        List<RoomResponse> out = service.listJoined(me);

        assertThat(out).extracting(RoomResponse::id).containsExactly(200L, 100L);
    }

    @Test
    void join_public_success() {
        long me = 1L;
        Room r = room(100L, 2L, false);

        // Service uses: rooms.findActiveByIdFetchHost(roomId)
        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));
        // Service uses: participants.existsMembership(roomId, userId)
        when(participants.existsMembership(100L, me)).thenReturn(false);

        service.join(100L, me, null);

        verify(participants).save(any(RoomParticipant.class));
    }

    @Test
    void join_private_requiresInvite() {
        long me = 1L;
        Room r = room(100L, 2L, true);
        r.setInviteCode("OKCODE");

        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));
        when(participants.existsMembership(100L, me)).thenReturn(false);

        assertThatThrownBy(() -> service.join(100L, me, "WRONG"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_INVALID_INVITE_CODE);
    }

    @Test
    void leave_host_forbidden() {
        long me = 1L;
        Room r = room(100L, me, false);

        // Service uses: rooms.findActiveByIdFetchHost(roomId)
        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        assertThatThrownBy(() -> service.leave(100L, me))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_HOST_CANNOT_LEAVE);
    }

    @Test
    void leave_success() {
        long me = 1L;
        Room r = room(100L, 2L, false);

        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        RoomParticipant rp = RoomParticipant.builder().room(r).userEntity(user(me)).build();
        // Service uses: participants.findMembership(roomId, userId)
        when(participants.findMembership(100L, me)).thenReturn(Optional.of(rp));

        service.leave(100L, me);

        verify(participants).delete(rp);
    }
}
