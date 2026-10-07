package io.github.mabals.pocketrand.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.mabals.pocketrand.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
