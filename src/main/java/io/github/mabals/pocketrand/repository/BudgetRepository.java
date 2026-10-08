package io.github.mabals.pocketrand.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.mabals.pocketrand.model.Budget;
import io.github.mabals.pocketrand.model.Category;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findAllByUserId(Long userId);

    Optional<Budget> findByUserIdAndCategory(Long userId, Category category);
}