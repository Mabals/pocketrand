package io.github.mabals.pocketrand.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import io.github.mabals.pocketrand.config.TaxTableProperties;
import io.github.mabals.pocketrand.config.TaxTableProperties.Bracket;
import io.github.mabals.pocketrand.dto.TaxEstimateRequest;
import io.github.mabals.pocketrand.dto.TaxEstimateResponse;
import io.github.mabals.pocketrand.model.SalaryPeriod;

@Service
public class TaxService {

    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TaxTableProperties table;

    public TaxService(TaxTableProperties table) {
        this.table = table;
    }

    public TaxEstimateResponse estimate(TaxEstimateRequest request) {
        BigDecimal annualGross = request.period() == SalaryPeriod.MONTHLY
                ? request.grossSalary().multiply(TWELVE)
                : request.grossSalary();
        annualGross = annualGross.setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyGross = annualGross.divide(TWELVE, 2, RoundingMode.HALF_UP);

        Bracket bracket = findBracket(annualGross);
        BigDecimal taxBeforeRebates = bracket.baseTax()
                .add(annualGross.subtract(bracket.from()).multiply(bracket.rate()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal rebates = rebatesFor(request.age());
        BigDecimal annualTax = taxBeforeRebates.subtract(rebates).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyTax = annualTax.divide(TWELVE, 2, RoundingMode.HALF_UP);

        BigDecimal monthlyUif = monthlyGross.multiply(table.uifRate()).min(table.uifMonthlyCap())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyTakeHome = monthlyGross.subtract(monthlyTax).subtract(monthlyUif);
        BigDecimal annualTakeHome = annualGross.subtract(annualTax).subtract(monthlyUif.multiply(TWELVE));

        return new TaxEstimateResponse(
                table.taxYear(),
                annualGross,
                monthlyGross,
                taxBeforeRebates,
                rebates,
                annualTax,
                monthlyTax,
                monthlyUif,
                monthlyTakeHome,
                annualTakeHome,
                annualTax.multiply(HUNDRED).divide(annualGross, 1, RoundingMode.HALF_UP),
                bracket.rate().multiply(HUNDRED).stripTrailingZeros(),
                "Estimate only, using SARS tables for the " + table.taxYear()
                        + " tax year. This is not tax advice.");
    }

    private Bracket findBracket(BigDecimal annualIncome) {
        Bracket result = table.brackets().getFirst();
        for (Bracket bracket : table.brackets()) {
            if (annualIncome.compareTo(bracket.from()) > 0) {
                result = bracket;
            }
        }
        return result;
    }

    private BigDecimal rebatesFor(int age) {
        BigDecimal rebates = table.primaryRebate();
        if (age >= 65) {
            rebates = rebates.add(table.secondaryRebate());
        }
        if (age >= 75) {
            rebates = rebates.add(table.tertiaryRebate());
        }
        return rebates;
    }
}
