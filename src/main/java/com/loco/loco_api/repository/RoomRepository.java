package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByInviteCode(String inviteCode);

    // 공개방 최신순
    List<Room> findByIsPrivateFalseOrderByCreatedAtDesc();

    // 비공개방 최신순
    List<Room> findByIsPrivateTrueOrderByCreatedAtDesc();

    // 호스트가 나인 방들 (내가 속한 방에 포함시키기 위함)
    List<Room> findByHost_IdOrderByCreatedAtDesc(Long hostId);

    // 삭제된 방은 제외
    Optional<Room> findByIdAndDeletedAtIsNull(Long id);
}
