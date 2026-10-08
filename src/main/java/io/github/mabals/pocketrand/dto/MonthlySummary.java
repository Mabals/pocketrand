package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;
import java.util.List;

import io.github.mabals.pocketrand.model.Category;

public record MonthlySummary(
        String month,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal net,
        int transactionCount,
        List<CategoryTotal> spendingByCategory,
        List<TransactionResponse> topExpenses,
        BigDecimal previousMonthExpenses,
        BigDecimal expenseChangePercent) {

    public record CategoryTotal(Category category, String label, BigDecimal amount, BigDecimal percentage) {
    }
}