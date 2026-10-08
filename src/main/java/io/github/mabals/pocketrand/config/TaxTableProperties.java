package io.github.mabals.pocketrand.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pocketrand.tax")
public record TaxTableProperties(
        int taxYear,
        List<Bracket> brackets,
        BigDecimal primaryRebate,
        BigDecimal secondaryRebate,
        BigDecimal tertiaryRebate,
        BigDecimal uifRate,
        BigDecimal uifMonthlyCap) {

    public record Bracket(BigDecimal from, BigDecimal baseTax, BigDecimal rate) {
    }
}
