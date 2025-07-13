package com.loco.loco_api.domain.tag;

import com.loco.loco_api.domain.room.Room;
import jakarta.persistence.*;
import lombok.*;

/**
 * 방 태그 엔티티
 */
@Entity
@Table(name = "room_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomTag {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private Room room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tag_id")
  private Tag tag;
}
