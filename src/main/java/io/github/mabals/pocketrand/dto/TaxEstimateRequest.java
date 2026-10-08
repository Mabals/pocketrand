package io.github.mabals.pocketrand.dto;

import java.math.BigDecimal;

import io.github.mabals.pocketrand.model.SalaryPeriod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TaxEstimateRequest(
        @NotNull(message = "Gross salary is required")
        @Positive(message = "Gross salary must be more than R0")
        BigDecimal grossSalary,

        @NotNull(message = "Period is required (MONTHLY or ANNUAL)")
        SalaryPeriod period,

        @NotNull(message = "Age is required")
        @Min(value = 15, message = "Age must be at least 15")
        @Max(value = 120, message = "Age must be 120 or less")
        Integer age) {
}
