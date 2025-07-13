package com.loco.loco_api.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 유저 엔티티
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String oauthId; // 예: Google ID
  private String provider; // google, kakao 등

  private String nickname;
  private String profileImageUrl;

  private LocalDateTime createdAt;
}
