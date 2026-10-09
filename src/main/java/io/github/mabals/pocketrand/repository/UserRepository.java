package io.github.mabals.pocketrand.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.mabals.pocketrand.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByEmailEndingWithAndCreatedAtBefore(String emailSuffix, Instant cutoff);
}
