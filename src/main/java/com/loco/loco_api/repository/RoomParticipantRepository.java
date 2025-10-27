package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.room.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    boolean existsByRoom_IdAndUserEntity_Id(Long roomId, Long userId);

    /**
     * 특정 방-유저 조합의 멤버십 존재 여부 (소프트삭제 제외)
     */
    @Query("SELECT (COUNT(rp) > 0) FROM RoomParticipant rp WHERE rp.room.id = :roomId AND rp.userEntity.id = :userId AND rp.deletedAt IS NULL")
    boolean existsMembership(Long roomId, Long userId);

    /**
     * 특정 방-유저 조합의 멤버십 단건 조회 (필요 시 room 즉시 로딩)
     */
    @Query("SELECT rp FROM RoomParticipant rp LEFT JOIN FETCH rp.room r WHERE r.id = :roomId AND rp.userEntity.id = :userId AND rp.deletedAt IS NULL")
    Optional<RoomParticipant> findMembership(Long roomId, Long userId);

    /**
     * 내가 참여한 방 목록을 Room으로 직접 변환 (host 즉시 로딩 + 소프트삭제 제외 + 최신순)
     */
    @Query("SELECT r FROM RoomParticipant rp LEFT JOIN rp.room r LEFT JOIN FETCH r.host h WHERE rp.userEntity.id = :userId AND r.deletedAt IS NULL AND rp.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Room> findJoinedRoomsBy(Long userId);
}
