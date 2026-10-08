package io.github.mabals.pocketrand.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.mabals.pocketrand.dto.MonthlySummary;
import io.github.mabals.pocketrand.dto.TransactionResponse;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.Transaction;
import io.github.mabals.pocketrand.repository.TransactionRepository;

@Service
public class SummaryService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TransactionRepository repository;

    public SummaryService(TransactionRepository repository) {
        this.repository = repository;
    }

    public MonthlySummary getMonthlySummary(Long userId, YearMonth month) {
        List<Transaction> current = findForMonth(userId, month);
        List<Transaction> previous = findForMonth(userId, month.minusMonths(1));

        BigDecimal income = totalIncome(current);
        BigDecimal expenses = totalExpenses(current);
        BigDecimal previousExpenses = totalExpenses(previous);

        Map<Category, BigDecimal> byCategory = current.stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        () -> new EnumMap<>(Category.class),
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)));

        List<MonthlySummary.CategoryTotal> spendingByCategory = byCategory.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> new MonthlySummary.CategoryTotal(
                        entry.getKey(),
                        entry.getKey().getLabel(),
                        entry.getValue(),
                        percentOf(entry.getValue(), expenses)))
                .toList();

        List<TransactionResponse> topExpenses = current.stream()
                .filter(Transaction::isExpense)
                .sorted(Comparator.comparing(Transaction::getAmount))
                .limit(3)
                .map(TransactionResponse::from)
                .toList();

        BigDecimal expenseChange = previousExpenses.signum() == 0
                ? null
                : expenses.subtract(previousExpenses).multiply(HUNDRED)
                        .divide(previousExpenses, 1, RoundingMode.HALF_UP);

        return new MonthlySummary(
                month.toString(),
                income,
                expenses,
                income.subtract(expenses),
                current.size(),
                spendingByCategory,
                topExpenses,
                previousExpenses,
                expenseChange);
    }

    private List<Transaction> findForMonth(Long userId, YearMonth month) {
        return repository.findAllByUserIdAndDateBetween(userId, month.atDay(1), month.atEndOfMonth());
    }

    private static BigDecimal totalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .filter(amount -> amount.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal totalExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal percentOf(BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(HUNDRED).divide(whole, 1, RoundingMode.HALF_UP);
    }
}