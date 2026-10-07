package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.github.mabals.pocketrand.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must be 255 characters or fewer")
        String description,

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        Category category) {
}