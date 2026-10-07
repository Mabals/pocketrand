package io.github.mabals.pocketrand.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.mabals.pocketrand.model.Transaction;

import java.util.Optional;
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByUserIdOrderByDateDescIdDesc(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
