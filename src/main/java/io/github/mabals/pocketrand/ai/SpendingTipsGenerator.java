package io.github.mabals.pocketrand.ai;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.mabals.pocketrand.dto.BudgetResponse;
import io.github.mabals.pocketrand.dto.MonthlySummary;
import io.github.mabals.pocketrand.util.Money;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SpendingTipsGenerator {

    private static final Logger log = LoggerFactory.getLogger(SpendingTipsGenerator.class);
    private static final int MAX_TIP_LENGTH = 200;

    private final GeminiClient gemini;
    private final JsonMapper jsonMapper;

    public SpendingTipsGenerator(GeminiClient gemini, JsonMapper jsonMapper) {
        this.gemini = gemini;
        this.jsonMapper = jsonMapper;
    }

    record TipsResult(List<String> tips) {
    }

    /** Returns up to 3 AI tips, or an empty list if the AI is unavailable. */
    public List<String> generate(MonthlySummary summary, List<BudgetResponse> budgets) {
        if (!gemini.isEnabled()) {
            return List.of();
        }
        String prompt = """
                You are a friendly money coach for South Africans.
                Using ONLY the facts below, write up to 3 short, practical tips about this month's spending.
                Rules:
                - Each tip is one sentence of at most 25 words.
                - Use rands written like R1,234.56, and only numbers that appear in the facts.
                - Be encouraging, not judgemental. Don't recommend specific financial products or companies.

                Facts:
                %s
                """.formatted(describe(summary, budgets));
        try {
            String json = gemini.generateJson(prompt, Map.of(
                    "type", "OBJECT",
                    "properties", Map.of("tips", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))),
                    "required", List.of("tips")));
            return jsonMapper.readValue(json, TipsResult.class).tips().stream()
                    .map(String::trim)
                    .filter(tip -> !tip.isBlank() && tip.length() <= MAX_TIP_LENGTH)
                    .limit(3)
                    .toList();
        } catch (RuntimeException e) {
            log.warn("AI tips failed, using rule-based tips: {}", e.getMessage());
            return List.of();
        }
    }

    private static String describe(MonthlySummary summary, List<BudgetResponse> budgets) {
        StringBuilder facts = new StringBuilder();
        facts.append("Month: ").append(summary.month()).append('\n');
        facts.append("Income: ").append(Money.rands(summary.income())).append('\n');
        facts.append("Spending: ").append(Money.rands(summary.expenses())).append('\n');
        facts.append("Left over: ").append(Money.rands(summary.net())).append('\n');
        if (summary.expenseChangePercent() != null) {
            facts.append("Change in spending from last month: ")
                 .append(summary.expenseChangePercent()).append("%\n");
        }
        facts.append("Spending by category:\n");
        for (MonthlySummary.CategoryTotal category : summary.spendingByCategory()) {
            facts.append("- ").append(category.label()).append(": ").append(Money.rands(category.amount()))
                 .append(" (").append(category.percentage()).append("% of spending)\n");
        }
        if (!budgets.isEmpty()) {
            facts.append("Budgets:\n");
            for (BudgetResponse budget : budgets) {
                facts.append("- ").append(budget.label()).append(": limit ").append(Money.rands(budget.monthlyLimit()))
                     .append(", spent ").append(Money.rands(budget.spent()))
                     .append(", status ").append(budget.status()).append('\n');
            }
        }
        return facts.toString();
    }
}