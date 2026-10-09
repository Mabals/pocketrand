package io.github.mabals.pocketrand.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.model.Budget;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.model.CategorySource;
import io.github.mabals.pocketrand.model.Transaction;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.BudgetRepository;
import io.github.mabals.pocketrand.repository.TransactionRepository;
import io.github.mabals.pocketrand.repository.UserRepository;

@Service
public class DemoDataService {

    public static final String DEMO_EMAIL_DOMAIN = "@demo.invalid";

    private record DemoTransaction(int day, String description, String amount,
                                   Category category, CategorySource source) {
    }

    private static final List<DemoTransaction> MONTH_TEMPLATE = List.of(
            new DemoTransaction(1, "SALARY", "25000.00", Category.INCOME, CategorySource.RULE),
            new DemoTransaction(2, "RENT PAYMENT", "-7500.00", Category.HOUSING, CategorySource.RULE),
            new DemoTransaction(3, "GROCERIES", "-1250.50", Category.GROCERIES, CategorySource.RULE),
            new DemoTransaction(5, "TAXI FARE", "-185.00", Category.TRANSPORT, CategorySource.RULE),
            new DemoTransaction(6, "AIRTIME PURCHASE", "-99.00", Category.AIRTIME_DATA, CategorySource.RULE),
            new DemoTransaction(8, "CORNER CAFE", "-145.00", Category.EATING_OUT, CategorySource.AI),
            new DemoTransaction(10, "PREPAID ELECTRICITY", "-650.00", Category.UTILITIES, CategorySource.RULE),
            new DemoTransaction(13, "MOVIE TICKETS", "-199.00", Category.ENTERTAINMENT, CategorySource.RULE),
            new DemoTransaction(16, "GROCERIES", "-980.25", Category.GROCERIES, CategorySource.RULE),
            new DemoTransaction(18, "BUS TICKET", "-25.00", Category.TRANSPORT, CategorySource.AI),
            new DemoTransaction(20, "PHARMACY", "-230.00", Category.HEALTH, CategorySource.RULE),
            new DemoTransaction(21, "FREELANCE PAYMENT", "3500.00", Category.INCOME, CategorySource.RULE),
            new DemoTransaction(23, "BURGER JOINT", "-120.00", Category.EATING_OUT, CategorySource.AI),
            new DemoTransaction(25, "MONTHLY ACCOUNT FEE", "-75.00", Category.FEES, CategorySource.RULE),
            new DemoTransaction(27, "CLOTHING STORE", "-420.00", Category.SHOPPING, CategorySource.USER));

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataService(UserRepository userRepository, TransactionRepository transactionRepository,
                           BudgetRepository budgetRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createDemoUser() {
        String email = "demo-" + UUID.randomUUID().toString().substring(0, 8) + DEMO_EMAIL_DOMAIN;
        String unusablePassword = passwordEncoder.encode(UUID.randomUUID().toString());
        User user = userRepository.save(new User("Demo User", email, unusablePassword));

        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        addMonth(user, thisMonth.minusMonths(1), new BigDecimal("0.90"), 31);
        addMonth(user, thisMonth, BigDecimal.ONE, today.getDayOfMonth());

        budgetRepository.saveAll(List.of(
                new Budget(user, Category.HOUSING, new BigDecimal("8000.00")),
                new Budget(user, Category.GROCERIES, new BigDecimal("2000.00")),
                new Budget(user, Category.EATING_OUT, new BigDecimal("250.00")),
                new Budget(user, Category.TRANSPORT, new BigDecimal("400.00"))));
        return user;
    }

    private void addMonth(User user, YearMonth month, BigDecimal expenseFactor, int lastDay) {
        List<Transaction> transactions = MONTH_TEMPLATE.stream()
                .filter(t -> t.day() <= lastDay && t.day() <= month.lengthOfMonth())
                .map(t -> {
                    BigDecimal amount = new BigDecimal(t.amount());
                    if (amount.signum() < 0) {
                        amount = amount.multiply(expenseFactor).setScale(2, RoundingMode.HALF_UP);
                    }
                    return new Transaction(user, month.atDay(t.day()), t.description(), amount,
                            t.category(), t.source());
                })
                .toList();
        transactionRepository.saveAll(transactions);
    }
}