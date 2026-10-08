package io.github.mabals.pocketrand.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.mabals.pocketrand.dto.BudgetResponse;
import io.github.mabals.pocketrand.dto.MonthlySummary;
import io.github.mabals.pocketrand.model.BudgetStatus;
import io.github.mabals.pocketrand.util.Money;

@Component
public class RuleBasedTips {

    private static final BigDecimal BIG_SHARE_PERCENT = BigDecimal.valueOf(50);
    private static final BigDecimal BIG_CHANGE_PERCENT = BigDecimal.valueOf(10);

    public List<String> generate(MonthlySummary summary, List<BudgetResponse> budgets) {
        List<String> tips = new ArrayList<>();

        if (summary.net().signum() < 0) {
            tips.add("You spent " + Money.rands(summary.net().abs()) + " more than you earned this month.");
        }

        budgets.stream()
                .filter(budget -> budget.status() == BudgetStatus.OVER_LIMIT)
                .forEach(budget -> tips.add("You're " + Money.rands(budget.remaining().abs())
                        + " over your " + budget.label() + " budget."));

        if (!summary.spendingByCategory().isEmpty()) {
            MonthlySummary.CategoryTotal biggest = summary.spendingByCategory().getFirst();
            if (biggest.percentage().compareTo(BIG_SHARE_PERCENT) >= 0) {
                tips.add(biggest.label() + " took " + biggest.percentage() + "% of your spending this month.");
            }
        }

        BigDecimal change = summary.expenseChangePercent();
        if (change != null && change.compareTo(BIG_CHANGE_PERCENT) >= 0) {
            tips.add("Your spending was up " + change + "% on last month.");
        }

        if (summary.net().signum() > 0) {
            tips.add("You kept " + Money.rands(summary.net())
                    + " of your income. Consider moving some of it into savings.");
        }

        return tips.stream().limit(3).toList();
    }
}