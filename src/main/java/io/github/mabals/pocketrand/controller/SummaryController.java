package io.github.mabals.pocketrand.controller;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.mabals.pocketrand.dto.MonthlySummary;
import io.github.mabals.pocketrand.service.SummaryService;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    public MonthlySummary getSummary(@RequestParam(required = false) String month,
                                     @AuthenticationPrincipal Jwt jwt) {
        return summaryService.getMonthlySummary(Long.valueOf(jwt.getSubject()), parseMonth(month));
    }

    private YearMonth parseMonth(String month) {
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