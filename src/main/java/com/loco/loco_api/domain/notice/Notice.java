package com.loco.loco_api.domain.notice;

import com.loco.loco_api.common.entity.UserAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notices",
        indexes = {
                @Index(name = "idx_notices_deleted", columnList = "deleted"),
                @Index(name = "idx_notices_deleted_id", columnList = "deleted,id") // 페이지네이션/정렬 최적화
        }
)
@Getter
@SQLDelete(sql = "UPDATE notices SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted = false")
public class Notice extends UserAuditableEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private boolean deleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected Notice() {}

  private Notice(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public static Notice of(String title, String content) {
    return new Notice(title, content);
  }

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }

  /**
   * 애플리케이션 메모리 상 엔티티 상태만 맞춰둠.
   * deletedAt은 DB에서 CURRENT_TIMESTAMP로 기록됨.
   */
  @PreRemove
  public void onSoftDelete() {
    this.deleted = true;
    // deletedAt은 DB가 기록하므로 여기서는 건드리지 않음
  }
}
