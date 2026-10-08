package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;

public record TaxEstimateResponse(
        int taxYear,
        BigDecimal annualGross,
        BigDecimal monthlyGross,
        BigDecimal annualTaxBeforeRebates,
        BigDecimal rebates,
        BigDecimal annualTax,
        BigDecimal monthlyTax,
        BigDecimal monthlyUif,
        BigDecimal monthlyTakeHome,
        BigDecimal annualTakeHome,
        BigDecimal effectiveRatePercent,
        BigDecimal marginalRatePercent,
        String note) {
}
