package io.github.mabals.pocketrand.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.mabals.pocketrand.dto.MonthlySummary;
import io.github.mabals.pocketrand.service.SummaryService;
import io.github.mabals.pocketrand.util.MonthParser;

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
        return summaryService.getMonthlySummary(Long.valueOf(jwt.getSubject()), MonthParser.parseOrCurrent(month));
    }
}