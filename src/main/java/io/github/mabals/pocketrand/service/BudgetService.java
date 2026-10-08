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
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.dto.BudgetResponse;
import io.github.mabals.pocketrand.exception.BudgetNotFoundException;
import io.github.mabals.pocketrand.model.Budget;
import io.github.mabals.pocketrand.model.BudgetStatus;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.Transaction;
import io.github.mabals.pocketrand.repository.BudgetRepository;
import io.github.mabals.pocketrand.repository.TransactionRepository;
import io.github.mabals.pocketrand.repository.UserRepository;

@Service
public class BudgetService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository, TransactionRepository transactionRepository,
                         UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<BudgetResponse> getBudgets(Long userId, YearMonth month) {
        Map<Category, BigDecimal> spending = spendingByCategory(userId, month);
        return budgetRepository.findAllByUserId(userId).stream()
                .map(budget -> toResponse(budget, spending))
                .sorted(Comparator.comparing(BudgetResponse::percentUsed).reversed())
                .toList();
    }

    @Transactional
    public BudgetResponse setBudget(Long userId, Category category, BigDecimal monthlyLimit) {
        if (category == Category.INCOME) {
            throw new IllegalArgumentException("Budgets can only be set for expense categories");
        }

        Budget budget = budgetRepository.findByUserIdAndCategory(userId, category)
                .map(existing -> {
                    existing.changeLimit(monthlyLimit);
                    return existing;
                })
                .orElseGet(() -> budgetRepository.save(
                        new Budget(userRepository.getReferenceById(userId), category, monthlyLimit)));

        return toResponse(budget, spendingByCategory(userId, YearMonth.now()));
    }

    @Transactional
    public void deleteBudget(Long userId, Category category) {
        Budget budget = budgetRepository.findByUserIdAndCategory(userId, category)
                .orElseThrow(() -> new BudgetNotFoundException(category));
        budgetRepository.delete(budget);
    }

    private Map<Category, BigDecimal> spendingByCategory(Long userId, YearMonth month) {
        return transactionRepository
                .findAllByUserIdAndDateBetween(userId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        () -> new EnumMap<>(Category.class),
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)));
    }

    private BudgetResponse toResponse(Budget budget, Map<Category, BigDecimal> spending) {
        BigDecimal limit = budget.getMonthlyLimit();
        BigDecimal spent = spending.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
        BigDecimal percentUsed = spent.multiply(HUNDRED).divide(limit, 1, RoundingMode.HALF_UP);

        return new BudgetResponse(
                budget.getCategory(),
                budget.getCategory().getLabel(),
                limit,
                spent,
                limit.subtract(spent),
                percentUsed,
                BudgetStatus.from(percentUsed));
    }
}
