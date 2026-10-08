package io.github.mabals.pocketrand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.mabals.pocketrand.config.TaxTableProperties;
import io.github.mabals.pocketrand.config.TaxTableProperties.Bracket;
import io.github.mabals.pocketrand.dto.TaxEstimateRequest;
import io.github.mabals.pocketrand.dto.TaxEstimateResponse;
import io.github.mabals.pocketrand.model.SalaryPeriod;

class TaxServiceTest {

    private final TaxService taxService = new TaxService(sars2027Table());

    @Test
    void thirtyThousandMonthlyAtAge30() {
        TaxEstimateResponse result = estimate("30000", SalaryPeriod.MONTHLY, 30);
        assertEquals(new BigDecimal("56172.00"), result.annualTax());
        assertEquals(new BigDecimal("4681.00"), result.monthlyTax());
        assertEquals(new BigDecimal("177.12"), result.monthlyUif());
        assertEquals(new BigDecimal("25141.88"), result.monthlyTakeHome());
    }

    @Test
    void lowIncomeIsFullyCoveredByTheRebate() {
        TaxEstimateResponse result = estimate("7000", SalaryPeriod.MONTHLY, 30);
        assertEquals(new BigDecimal("0.00"), result.annualTax());
    }

    @Test
    void topBracketIncome() {
        TaxEstimateResponse result = estimate("2000000", SalaryPeriod.ANNUAL, 30);
        assertEquals(new BigDecimal("720969.00"), result.annualTaxBeforeRebates());
        assertEquals(new BigDecimal("703149.00"), result.annualTax());
    }

    @Test
    void secondaryRebateFromAge65() {
        TaxEstimateResponse result = estimate("360000", SalaryPeriod.ANNUAL, 70);
        assertEquals(new BigDecimal("46407.00"), result.annualTax());
    }

    @Test
    void incomeExactlyOnTheBoundaryStaysInTheLowerBracket() {
        TaxEstimateResponse result = estimate("245100", SalaryPeriod.ANNUAL, 30);
        assertEquals(new BigDecimal("44118.00"), result.annualTaxBeforeRebates());
    }

    private TaxEstimateResponse estimate(String salary, SalaryPeriod period, int age) {
        return taxService.estimate(new TaxEstimateRequest(new BigDecimal(salary), period, age));
    }

    private static TaxTableProperties sars2027Table() {
        return new TaxTableProperties(
                2027,
                List.of(
                        bracket("0", "0", "0.18"),
                        bracket("245100", "44118", "0.26"),
                        bracket("383100", "79998", "0.31"),
                        bracket("530200", "125599", "0.36"),
                        bracket("695800", "185215", "0.39"),
                        bracket("887000", "259783", "0.41"),
                        bracket("1878600", "666339", "0.45")),
                new BigDecimal("17820"),
                new BigDecimal("9765"),
                new BigDecimal("3249"),
                new BigDecimal("0.01"),
                new BigDecimal("177.12"));
    }

    private static Bracket bracket(String from, String baseTax, String rate) {
        return new Bracket(new BigDecimal(from), new BigDecimal(baseTax), new BigDecimal(rate));
    }
}