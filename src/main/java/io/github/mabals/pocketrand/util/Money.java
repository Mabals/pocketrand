package io.github.mabals.pocketrand.util;

import java.math.BigDecimal;
import java.util.Locale;

public final class Money {

    private Money() {
        // Utility class: not meant to be instantiated
    }

    public static String rands(BigDecimal amount) {
        return String.format(Locale.ENGLISH, "R%,.2f", amount);
    }
}