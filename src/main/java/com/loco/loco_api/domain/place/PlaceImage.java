package com.loco.loco_api.domain.place;

import com.loco.loco_api.common.entity.UserAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 장소 이미지 엔티티
 */
@Entity
@Table(name = "place_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaceImage extends UserAuditableEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  private Place place;
}

