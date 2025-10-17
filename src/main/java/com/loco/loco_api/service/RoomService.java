package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.request.RoomUpdateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.RoomRepository;
import com.loco.loco_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository rooms;
    private final UserRepository users;
    private final SecureRandom random = new SecureRandom();
    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no confusing chars

    // 방 상세 조회
    public RoomResponse getDetail(Long roomId) {
        Room room = rooms.findById(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));
        return RoomResponse.from(room);
    }

    // 공개방 목록
    public List<RoomResponse> listPublic() {
        return rooms.findByIsPrivateFalseOrderByCreatedAtDesc().stream().map(RoomResponse::from).toList();
    }

    // 비공개방 목록 (created
    public List<RoomResponse> listPrivate() {
        return rooms.findByIsPrivateTrueOrderByCreatedAtDesc().stream().map(RoomResponse::from).toList();
    }

    // 방 생성
    @Transactional
    public RoomResponse create(RoomCreateRequest req, Long hostId) {
        UserEntity host = users.findById(hostId).orElseThrow(() -> new IllegalArgumentException("Host user not found: " + hostId));

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
        String code;
        while (true) {
            code = randomCode(len);
            if (!rooms.existsByInviteCode(code)) {
                return code;
            }
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
        Room room = rooms.findById(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // 권한 체크: 호스트만
        if (!room.getHost().getId().equals(requesterId)) {
            throw new CustomException(ErrorCode.ROOM_NOT_HOST);
        }

        // 부분 업데이트
        if (req.name() != null && !req.name().isBlank()) {
            room.setName(req.name());
        }
        if (req.name() != null) {
            room.setDescription(req.description());
        }
        if (req.isPrivate() != null) {
            room.setPrivate(req.isPrivate());
        }
        if (req.thumbnail() != null) {
            room.setThumbnail(req.thumbnail());
        }

        Room saved = rooms.save(room);

        return RoomResponse.from(room);
    }

    @Transactional
    public void delete(Long roomId, Long requesterId) {
        Room room = rooms.findById(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getHost().getId().equals(requesterId)) {
            throw new CustomException(ErrorCode.ROOM_NOT_HOST);
        }

        rooms.delete(room);
    }
}
