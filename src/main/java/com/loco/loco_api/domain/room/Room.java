package com.loco.loco_api.domain.room;

import com.loco.loco_api.common.entity.BaseEntity;
import com.loco.loco_api.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * 방 엔티티
 */
@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE rooms SET deleted_at = NOW(), invite_code = NULL WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
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

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}

