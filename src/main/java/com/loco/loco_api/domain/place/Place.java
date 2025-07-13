package com.loco.loco_api.domain.place;

import com.loco.loco_api.common.entity.UserAuditableEntity;
import com.loco.loco_api.domain.room.Room;
import jakarta.persistence.*;
import lombok.*;

/**
 * 장소 엔티티
 */
@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Place extends UserAuditableEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private String address;

  private double latitude;
  private double longitude;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private Room room;
}

