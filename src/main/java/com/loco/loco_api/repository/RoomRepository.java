package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByInviteCode(String inviteCode);

    // 공개방 최신순
    List<Room> findByIsPrivateFalseOrderByCreatedAtDesc();

    // 비공개방 최신순
    List<Room> findByIsPrivateTrueOrderByCreatedAtDesc();
}
