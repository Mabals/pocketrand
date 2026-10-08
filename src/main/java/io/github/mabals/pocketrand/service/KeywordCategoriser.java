package io.github.mabals.pocketrand.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import io.github.mabals.pocketrand.model.Category;

import io.github.mabals.pocketrand.model.CategorySource;

@Component
public class KeywordCategoriser {

    private record Rule(Category category, List<Pattern> patterns) {
        boolean matches(String text) {
            return patterns.stream().anyMatch(pattern -> pattern.matcher(text).find());
        }
    }

    // Checked top to bottom: the first matching rule wins, so more specific rules come first.
    private static final List<Rule> RULES = List.of(
            rule(Category.EATING_OUT, "uber eats", "mr d", "kfc", "mcdonalds", "nandos", "steers",
                    "debonairs", "restaurant", "takeaway"),
            rule(Category.FEES, "bank charges", "service fee", "monthly fee", "admin fee", "fee"),
            rule(Category.TRANSFERS, "transfer", "eft to", "send money"),
            rule(Category.HOUSING, "rent", "bond", "levy"),
            rule(Category.GROCERIES, "groceries", "checkers", "shoprite", "pick n pay", "spar",
                    "woolworths", "food lovers"),
            rule(Category.TRANSPORT, "uber", "bolt", "taxi", "petrol", "fuel", "gautrain", "parking"),
            rule(Category.AIRTIME_DATA, "airtime", "data bundle", "vodacom", "mtn", "telkom", "cell c"),
            rule(Category.ENTERTAINMENT, "netflix", "showmax", "spotify", "dstv", "cinema", "movie"),
            rule(Category.UTILITIES, "electricity", "prepaid elec", "water", "municipal", "rates"),
            rule(Category.HEALTH, "pharmacy", "clicks", "dis-chem", "doctor", "medical aid", "hospital"),
            rule(Category.EDUCATION, "tuition", "school fees", "university", "course"),
            rule(Category.SHOPPING, "takealot", "mr price", "pep", "edgars", "ackermans", "clothing"),
            rule(Category.INCOME, "salary", "bursary", "interest", "refund"));

    public Optional<Category> categorise(String description, BigDecimal amount) {
        String text = description.toLowerCase();

        Optional<Category> match = RULES.stream()
                .filter(rule -> rule.matches(text))
                .map(Rule::category)
                .findFirst();

        if (match.isPresent()) {
            return match;
        }
        // Money coming in that no rule recognises is treated as income.
        return amount.signum() > 0 ? Optional.of(Category.INCOME) : Optional.empty();
    }

    public record CategoryDecision(Category category, CategorySource source) {
    }

    public CategoryDecision decide(Category requested, String description, BigDecimal amount) {
        if (requested != null) {
            return new CategoryDecision(requested, CategorySource.USER);
        }
        Category category = categorise(description, amount).orElse(Category.OTHER);
        return new CategoryDecision(category, CategorySource.RULE);
    }

    private static Rule rule(Category category, String... keywords) {
        List<Pattern> patterns = Arrays.stream(keywords)
                .map(keyword -> Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b"))
                .toList();
        return new Rule(category, patterns);
    }
}