package io.github.mabals.pocketrand.exception;

import io.github.mabals.pocketrand.model.Category;

public class BudgetNotFoundException extends RuntimeException {

    public BudgetNotFoundException(Category category) {
        super("No budget set for " + category.getLabel());
    }
}