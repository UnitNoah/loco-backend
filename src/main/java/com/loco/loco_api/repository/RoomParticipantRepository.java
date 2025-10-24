package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.RoomParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    boolean existsByRoom_IdAndUserEntity_Id(Long roomId, Long userId);

    Optional<RoomParticipant> findByRoom_IdAndUserEntity_Id(Long roomId, Long userId);

    @EntityGraph(attributePaths = "room") // avoid N+1 when accessing roomParticipant.getRoom()
    List<RoomParticipant> findByUserEntity_IdAndRoom_DeletedAtIsNullOrderByRoom_CreatedAtDesc(Long userId);}
