package com.loco.loco_api.repository;

import com.loco.loco_api.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
  Optional<UserEntity> findByProviderAndOauthId(String provider, String oauthId);
}
