package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByInviteCode(String inviteCode);

    /**
     * 단건 상세 (host 즉시 로딩 + 소프트삭제 제외)
     */
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.host h WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Room> findActiveByIdFetchHost(Long id);

    /**
     * 공개방 목록 (host 즉시 로딩)
     */
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.host h WHERE r.isPrivate = false AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Room> findPublicOrderByCreatedAtDesc();

    /**
     * 비공개방 목록 (host 즉시 로딩)
     */
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.host h WHERE r.isPrivate = true AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Room> findPrivateOrderByCreatedAtDesc();

    /**
     * 내가 호스트인 방 목록 (host 즉시 로딩)
     */

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.host h WHERE h.id = :hostId and r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Room> findHostBy(Long hostId);
}
