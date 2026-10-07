package io.github.mabals.pocketrand.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_source", nullable = false, length = 10)
    private CategorySource categorySource;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {
        // Required by JPA
    }

    public Transaction(LocalDate date, String description, BigDecimal amount,
                       Category category, CategorySource categorySource) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.categorySource = categorySource;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void updateDetails(LocalDate date, String description, BigDecimal amount) {
        this.date = date;
        this.description = description;
        this.amount = amount;
    }

    public void changeCategory(Category category, CategorySource source) {
        this.category = category;
        this.categorySource = source;
    }

    public boolean isExpense() {
        return amount.signum() < 0;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public CategorySource getCategorySource() {
        return categorySource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
