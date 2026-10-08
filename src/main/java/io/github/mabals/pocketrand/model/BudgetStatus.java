package io.github.mabals.pocketrand.model;

import java.math.BigDecimal;

public enum BudgetStatus {
    ON_TRACK,
    NEAR_LIMIT,
    OVER_LIMIT;

    private static final BigDecimal NEAR_LIMIT_PERCENT = BigDecimal.valueOf(80);
    private static final BigDecimal FULL_PERCENT = BigDecimal.valueOf(100);

    public static BudgetStatus from(BigDecimal percentUsed) {
        if (percentUsed.compareTo(FULL_PERCENT) > 0) {
            return OVER_LIMIT;
        }
        if (percentUsed.compareTo(NEAR_LIMIT_PERCENT) >= 0) {
            return NEAR_LIMIT;
        }
        return ON_TRACK;
    }
}