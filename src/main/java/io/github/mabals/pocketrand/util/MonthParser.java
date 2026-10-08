package io.github.mabals.pocketrand.util;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public final class MonthParser {

    private MonthParser() {
        // Utility class: not meant to be instantiated
    }

    public static YearMonth parseOrCurrent(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Month must be in the format YYYY-MM, for example 2026-09");
        }
    }
}