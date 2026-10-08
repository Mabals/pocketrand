package io.github.mabals.pocketrand.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.mabals.pocketrand.model.Transaction;

import java.util.Optional;
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByUserIdOrderByDateDescIdDesc(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndDateAndDescriptionAndAmount(Long userId, LocalDate date,
                                                      String description, BigDecimal amount);

    List<Transaction> findAllByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}
