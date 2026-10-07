package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.CategorySource;
import io.github.mabals.pocketrand.model.Transaction;

public record TransactionResponse(
        Long id,
        LocalDate date,
        String description,
        BigDecimal amount,
        Category category,
        String categoryLabel,
        CategorySource categorySource) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getCategory().getLabel(),
                transaction.getCategorySource());
    }
}