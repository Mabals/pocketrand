package io.github.mabals.pocketrand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.mabals.pocketrand.model.Category;

class KeywordCategoriserTest {

    private final KeywordCategoriser categoriser = new KeywordCategoriser();

    @Test
    void uberEatsIsEatingOutNotTransport() {
        Optional<Category> result = categoriser.categorise("UBER EATS ORDER", new BigDecimal("-145.00"));
        assertEquals(Optional.of(Category.EATING_OUT), result);
    }

    @Test
    void uberRideIsTransport() {
        Optional<Category> result = categoriser.categorise("Uber trip to work", new BigDecimal("-85.00"));
        assertEquals(Optional.of(Category.TRANSPORT), result);
    }

    @Test
    void wholeWordsOnlySoCurrentIsNotRent() {
        Optional<Category> result = categoriser.categorise("CURRENT ACCOUNT FEE", new BigDecimal("-75.00"));
        assertEquals(Optional.of(Category.FEES), result);
    }

    @Test
    void unknownExpenseHasNoCategory() {
        Optional<Category> result = categoriser.categorise("Something unusual", new BigDecimal("-50.00"));
        assertEquals(Optional.empty(), result);
    }

    @Test
    void unknownMoneyInIsIncome() {
        Optional<Category> result = categoriser.categorise("Payment from client", new BigDecimal("3500.00"));
        assertEquals(Optional.of(Category.INCOME), result);
    }
}