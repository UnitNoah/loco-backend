package com.loco.loco_api.common.entity;

import com.loco.loco_api.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

/**
 * BaseEntity를 상속하는 생성자,수정자 엔티티
 * 분리이유는 생성일자와 수정일자만 필요한 엔티티가 존재하기 때문에 분리함
 */
@Getter
@MappedSuperclass
public abstract class UserAuditableEntity extends BaseEntity {

  @CreatedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  protected UserEntity createdBy;

  @LastModifiedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  protected UserEntity updatedBy;
}