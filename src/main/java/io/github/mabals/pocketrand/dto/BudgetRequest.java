package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BudgetRequest(
        @NotNull(message = "Monthly limit is required")
        @Positive(message = "Monthly limit must be more than R0")
        @Digits(integer = 10, fraction = 2, message = "Monthly limit can have at most 2 decimal places")
        BigDecimal monthlyLimit) {
}