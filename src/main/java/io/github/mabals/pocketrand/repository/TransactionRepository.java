package io.github.mabals.pocketrand.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.mabals.pocketrand.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByOrderByDateDescIdDesc();
}
