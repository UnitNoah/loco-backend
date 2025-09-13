package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByInviteCode(String inviteCode);
}
