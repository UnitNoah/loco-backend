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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceMembershipTest {

    @Mock RoomRepository rooms;
    @Mock UserRepository users;
    @Mock RoomParticipantRepository participants;

    @InjectMocks RoomService service;

    // ---- helpers ----
    private UserEntity user(long id) {
        return UserEntity.builder()
                .id(id)
                .nickname("user" + id)
                .profileImageUrl("https://img/" + id)
                .provider("google")
                .oauthId("x" + id)
                .build();
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

    private RoomParticipantRepository.RoomMemberCount mc(Long roomId, long cnt) {
        return new RoomParticipantRepository.RoomMemberCount() {
            @Override public Long getRoomId() { return roomId; }
            @Override public long getCnt() { return cnt; } // participants only (excludes host)
        };
    }

    // ---- tests ----

    @Test
    void listHosted_returnsHostRooms_sortedByCreatedAtDesc_andIncludesMemberCount() {
        long me = 1L;
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        Room room1 = room(10L, me, false); // older
        Room room2 = room(20L, me, true);  // newer

        room1.setCreatedAt(LocalDateTime.now().minusDays(1));
        room2.setCreatedAt(LocalDateTime.now());

        when(rooms.findHostBy(me)).thenReturn(List.of(room2, room1));

        // participants only: room2 has 2 members joined; room1 has 0 -> final counts = 3 and 1
        when(participants.countActiveByRoomIds(List.of(20L, 10L)))
                .thenReturn(List.of(mc(20L, 2), mc(10L, 0)));

        List<RoomResponse> out = service.listHosted(me);

        assertThat(out).extracting(RoomResponse::id).containsExactly(20L, 10L);
        assertThat(out).extracting(RoomResponse::memberCount).containsExactly(3, 1);
        verify(rooms).findHostBy(me);
        verify(participants).countActiveByRoomIds(List.of(20L, 10L));
    }

    @Test
    void listJoined_returnsJoinedRooms_sortedByCreatedAtDesc_andIncludesMemberCount() {
        long me = 1L;
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        Room joinedNew = room(200L, 99L, false);
        Room joinedOld = room(100L, 88L, true);

        joinedNew.setCreatedAt(LocalDateTime.now());
        joinedOld.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(participants.findJoinedRoomsBy(me)).thenReturn(List.of(joinedNew, joinedOld));

        // participants only: 200L->4, 100L->1  → final counts = 5, 2
        when(participants.countActiveByRoomIds(List.of(200L, 100L)))
                .thenReturn(List.of(mc(200L, 4), mc(100L, 1)));

        List<RoomResponse> out = service.listJoined(me);

        assertThat(out).extracting(RoomResponse::id).containsExactly(200L, 100L);
        assertThat(out).extracting(RoomResponse::memberCount).containsExactly(5, 2);
        verify(participants).findJoinedRoomsBy(me);
        verify(participants).countActiveByRoomIds(List.of(200L, 100L));
    }

    @Test
    void join_public_success_returnsResponseWithMemberCount() {
        long me = 1L;
        Room r = room(100L, 2L, false);

        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));
        when(participants.existsMembership(100L, me)).thenReturn(false);

        // after joining, participants-only count should include me → 1; final = 1 (participants) + 1 (host) = 2
        when(participants.countActiveByRoomIds(List.of(100L))).thenReturn(List.of(mc(100L, 1)));

        RoomResponse resp = service.join(100L, me, null);

        assertThat(resp.id()).isEqualTo(100L);
        assertThat(resp.memberCount()).isEqualTo(2);
        verify(participants).save(any(RoomParticipant.class));
        verify(participants).countActiveByRoomIds(List.of(100L));
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

        verify(participants, never()).save(any());
        verify(participants, never()).countActiveByRoomIds(any());
    }

    @Test
    void join_asHost_returnsRoomResponse_withoutSavingMembership_andMemberCountIsHostOnlyWhenNoParticipants() {
        long me = 1L;
        Room r = room(100L, me, false);

        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        // no participants → final = 0 + 1 host = 1
        when(participants.countActiveByRoomIds(List.of(100L))).thenReturn(List.of());

        RoomResponse resp = service.join(100L, me, null);

        assertThat(resp.id()).isEqualTo(100L);
        assertThat(resp.memberCount()).isEqualTo(1);
        verify(participants, never()).save(any());
        verify(participants).countActiveByRoomIds(List.of(100L));
    }

    @Test
    void join_alreadyJoined_conflict() {
        long me = 1L;
        Room r = room(100L, 2L, false);

        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));
        when(participants.existsMembership(100L, me)).thenReturn(true);

        assertThatThrownBy(() -> service.join(100L, me, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_ALREADY_JOINED);

        verify(participants, never()).save(any());
        verify(participants, never()).countActiveByRoomIds(any());
    }

    @Test
    void leave_host_forbidden() {
        long me = 1L;
        Room r = room(100L, me, false);

        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        assertThatThrownBy(() -> service.leave(100L, me))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_HOST_CANNOT_LEAVE);

        verify(participants, never()).delete(any());
        verify(participants, never()).countActiveByRoomIds(any());
    }

    @Test
    void leave_success_returnsResponseWithUpdatedMemberCount() {
        long me = 1L;
        Room r = room(100L, 2L, false);

        when(rooms.findActiveByIdFetchHost(100L)).thenReturn(Optional.of(r));
        when(users.findById(me)).thenReturn(Optional.of(user(me)));

        RoomParticipant rp = RoomParticipant.builder().room(r).userEntity(user(me)).build();
        when(participants.findMembership(100L, me)).thenReturn(Optional.of(rp));

        // after deletion there are 0 participants left → final = 0 + 1 host = 1
        when(participants.countActiveByRoomIds(List.of(100L))).thenReturn(List.of());

        RoomResponse resp = service.leave(100L, me);

        assertThat(resp.id()).isEqualTo(100L);
        assertThat(resp.memberCount()).isEqualTo(1);
        verify(participants).delete(rp);
        verify(participants).countActiveByRoomIds(List.of(100L));
    }
}
