package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.room.request.RoomCreateRequest;
import com.loco.loco_api.common.dto.room.response.RoomResponse;
import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.RoomRepository;
import com.loco.loco_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository rooms;
    private final UserRepository users;
    private final SecureRandom random = new SecureRandom();
    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no confusing chars

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

        return new RoomResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.isPrivate(),
                saved.getThumbnail(),
                saved.getHost().getId(),
                saved.getInviteCode()
        );
    }

    private String generateUniqueInviteCode(int len) {
        String code;
        do {
            code = randomCode(len);
        } while (rooms.existsByInviteCode(code));
        return code;
    }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
