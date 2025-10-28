package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.request.RoomUpdateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.room.RoomParticipant;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.RoomParticipantRepository;
import com.loco.loco_api.repository.RoomRepository;
import com.loco.loco_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository rooms;
    private final UserRepository users;
    private final RoomParticipantRepository participants;
    private final SecureRandom random = new SecureRandom();
    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no confusing chars

    // 공개 방 상세 조회
    public RoomResponse getPublicDetail(Long roomId) {
        Room room = rooms.findActiveByIdFetchHost(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));
        if (room.isPrivate()) throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        return RoomResponse.from(room);
    }

    // 비공개방 상세 조회
    public RoomResponse getPrivateDetail(Long roomId) {
        Room room = rooms.findActiveByIdFetchHost(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));
        if (!room.isPrivate()) throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        return RoomResponse.from(room);
    }

    // 공개방 목록
    public List<RoomResponse> listPublic() {
        return rooms.findPublicOrderByCreatedAtDesc().stream().map(RoomResponse::from).toList();
    }

    // 비공개방 목록 (created
    public List<RoomResponse> listPrivate() {
        return rooms.findPrivateOrderByCreatedAtDesc().stream().map(RoomResponse::from).toList();
    }

    // 방 생성
    @Transactional
    public RoomResponse create(RoomCreateRequest req, Long hostId) {
        UserEntity host = users.findById(hostId).orElseThrow(() -> new IllegalArgumentException("Host user not found: " + hostId));
// TODO: Make this into constant
        String invite = generateUniqueInviteCode(8);

        Room room = Room.builder().name(req.name())
                .description(req.description())
                .isPrivate(Boolean.TRUE.equals(req.isPrivate()))
                .inviteCode(invite)
                .thumbnail(req.thumbnail())
                .host(host)
                .build();

        Room saved = rooms.save(room);

        return RoomResponse.from(saved);
    }

    private String generateUniqueInviteCode(int len) {
        while (true) {
            String code = randomCode(len);
            if (!rooms.existsByInviteCode(code)) return code;
        }
    }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    // 방 정보 수정
    @Transactional
    public RoomResponse update(Long roomId, Long requesterId, RoomUpdateRequest req) {
        Room room = rooms.findActiveByIdFetchHost(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // 권한 체크: 호스트만
        if (!room.getHost().getId().equals(requesterId)) {
            throw new CustomException(ErrorCode.ROOM_NOT_HOST);
        }

        // 부분 업데이트
        if (req.name() != null && !req.name().isBlank()) room.setName(req.name());
        if (req.description() != null) room.setDescription(req.description());
        if (req.isPrivate() != null) room.setPrivate(req.isPrivate());
        if (req.thumbnail() != null) room.setThumbnail(req.thumbnail());

        Room saved = rooms.save(room);

        return RoomResponse.from(room);
    }

    @Transactional
    public Long delete(Long roomId, Long requesterId) {
        Room room = rooms.findActiveByIdFetchHost(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getHost().getId().equals(requesterId)) {
            throw new CustomException(ErrorCode.ROOM_NOT_HOST);
        }

        Long id = room.getId();
        rooms.delete(room);
        return room.getId();
    }

    // 내가 호스트인 방 목록 (createdAt DESC, soft-delete 제외)
    public List<RoomResponse> listHosted(Long userId) {
        users.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return rooms.findHostBy(userId).stream().map(RoomResponse::from).toList();
    }

    // 내가 참여자인 방 목록
    public List<RoomResponse> listJoined(Long userId) {
        users.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return participants.findJoinedRoomsBy(userId).stream().map(RoomResponse::from).toList();
    }

    // 방 참여
    @Transactional
    public RoomResponse join(Long roomId, Long userId, String inviteCode) {
        Room room = rooms.findActiveByIdFetchHost(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));
        users.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 호스트는 이미 구성원으로 간주
        if (room.getHost() != null && room.getHost().getId().equals(userId)) {
            return RoomResponse.from(room);
        }

        // 이미 참가한 유저면 에러
        if (participants.existsMembership(roomId, userId)) {
            throw new CustomException(ErrorCode.ROOM_ALREADY_JOINED);
        }

        // 비공개인 경우 초대코드 필요
        if (room.isPrivate()) {
            boolean codeOk = room.getInviteCode() != null && room.getInviteCode().equals(inviteCode);
            if (!codeOk) throw new CustomException(ErrorCode.ROOM_INVALID_INVITE_CODE);
        }

        RoomParticipant rp = RoomParticipant.builder()
                .room(room)
                .userEntity(users.getReferenceById(userId))
                .joinedAt(LocalDateTime.now())
                .build();

        participants.save(rp);

        return RoomResponse.from(room);
    }

    // 방 나가기 (호스트는 금지)
    @Transactional
    public RoomResponse leave(Long roomId, Long userId) {
        // 삭제된 방에서의 leave는 허용하지 않음(일관성 유지)
        Room room = rooms.findActiveByIdFetchHost(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));
        users.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 호스트는 나가기 금지
        if (room.getHost() != null && room.getHost().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ROOM_HOST_CANNOT_LEAVE);
        }

        RoomParticipant rp = participants.findMembership(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_PARTICIPANT_NOT_FOUND));

        participants.delete(rp);

        return RoomResponse.from(room);
    }
}
