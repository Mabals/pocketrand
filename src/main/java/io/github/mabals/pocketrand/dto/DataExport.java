package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.github.mabals.pocketrand.model.Category;

public record DataExport(
        Instant exportedAt,
        Profile profile,
        List<TransactionResponse> transactions,
        List<BudgetLimit> budgets) {

    public record Profile(String fullName, String email, Instant createdAt, Instant privacyAcceptedAt) {
    }

    public record BudgetLimit(Category category, BigDecimal monthlyLimit) {
    }
}