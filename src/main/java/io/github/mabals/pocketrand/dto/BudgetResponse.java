package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;

import io.github.mabals.pocketrand.model.BudgetStatus;
import io.github.mabals.pocketrand.model.Category;

public record BudgetResponse(
        Category category,
        String label,
        BigDecimal monthlyLimit,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal percentUsed,
        BudgetStatus status) {
}