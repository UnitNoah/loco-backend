package com.loco.loco_api.domain.user;

import com.loco.loco_api.common.entity.UserAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_provider_oauth", columnNames = {"provider","oauth_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity extends UserAuditableEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "oauth_id", nullable = false)
  private String oauthId;               // 공급자 고유 ID

  @Column(nullable = false)
  private String provider;              // google/naver/kakao

  @Column
  private String email;                 // 동의 안 하면 null

  @Column
  private String nickname;              // 표시 이름(없을 수 있음)

  private String profileImageUrl;

  @org.hibernate.annotations.CreationTimestamp
  @Column(updatable = false)
  private java.time.LocalDateTime createdAt;

  @org.hibernate.annotations.UpdateTimestamp
  private java.time.LocalDateTime updatedAt;

  /** null/blank는 덮어쓰지 않도록 방어 */
  public void updateProfile(String nickname, String profileImageUrl, String email) {
    if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
    if (profileImageUrl != null && !profileImageUrl.isBlank()) this.profileImageUrl = profileImageUrl;
    if (email != null && !email.isBlank()) this.email = email;
  }
}
