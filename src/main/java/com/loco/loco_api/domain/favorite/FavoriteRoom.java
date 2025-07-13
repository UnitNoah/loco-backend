package com.loco.loco_api.domain.favorite;

import com.loco.loco_api.domain.user.User;
import com.loco.loco_api.domain.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 엔티티 (방)
 */
@Entity
@Table(name = "favorite_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FavoriteRoom {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private Room room;
}
