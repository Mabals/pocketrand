package io.github.mabals.pocketrand.service;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.mabals.pocketrand.ai.SpendingTipsGenerator;
import io.github.mabals.pocketrand.dto.BudgetResponse;
import io.github.mabals.pocketrand.dto.MonthlySummary;
import io.github.mabals.pocketrand.dto.SpendingTips;

@Service
public class TipsService {

    private final SummaryService summaryService;
    private final BudgetService budgetService;
    private final SpendingTipsGenerator aiTips;
    private final RuleBasedTips ruleBasedTips;

    public TipsService(SummaryService summaryService, BudgetService budgetService,
                       SpendingTipsGenerator aiTips, RuleBasedTips ruleBasedTips) {
        this.summaryService = summaryService;
        this.budgetService = budgetService;
        this.aiTips = aiTips;
        this.ruleBasedTips = ruleBasedTips;
    }

    public SpendingTips getTips(Long userId, YearMonth month) {
        MonthlySummary summary = summaryService.getMonthlySummary(userId, month);
        if (summary.transactionCount() == 0) {
            return new SpendingTips(month.toString(),
                    List.of("No transactions for this month yet. Import a statement to get insights."), false);
        }

        List<BudgetResponse> budgets = budgetService.getBudgets(userId, month);

        List<String> tips = aiTips.generate(summary, budgets);
        if (!tips.isEmpty()) {
            return new SpendingTips(month.toString(), tips, true);
        }
        return new SpendingTips(month.toString(), ruleBasedTips.generate(summary, budgets), false);
    }
}