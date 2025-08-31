package com.loco.loco_api.domain.room;

import com.loco.loco_api.common.entity.BaseEntity;
import com.loco.loco_api.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 방 엔티티
 */
@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private boolean isPrivate;
  private String inviteCode;
  private String thumbnail;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "host_id")
  private UserEntity host;
}

