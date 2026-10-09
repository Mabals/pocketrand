package io.github.mabals.pocketrand.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.mabals.pocketrand.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteAllByUserId(Long userId);
}
