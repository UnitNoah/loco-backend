package com.loco.loco_api.domain.tag;

import jakarta.persistence.*;
import lombok.*;

/**
 * 다대다 매핑 방지용 태그 엔티티
 */
@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tag {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
}
