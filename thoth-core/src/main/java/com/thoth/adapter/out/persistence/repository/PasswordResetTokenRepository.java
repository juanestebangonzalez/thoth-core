package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    List<PasswordResetTokenEntity> findByUserIdAndUsedAtIsNull(UUID userId);
}
