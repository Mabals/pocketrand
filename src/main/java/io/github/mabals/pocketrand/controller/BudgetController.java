package io.github.mabals.pocketrand.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.mabals.pocketrand.dto.BudgetRequest;
import io.github.mabals.pocketrand.dto.BudgetResponse;
import io.github.mabals.pocketrand.model.Category;
import io.github.mabals.pocketrand.service.BudgetService;
import io.github.mabals.pocketrand.util.MonthParser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> getBudgets(@RequestParam(required = false) String month,
                                           @AuthenticationPrincipal Jwt jwt) {
        return budgetService.getBudgets(currentUserId(jwt), MonthParser.parseOrCurrent(month));
    }

    @PutMapping("/{category}")
    public BudgetResponse setBudget(@PathVariable Category category,
                                    @Valid @RequestBody BudgetRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return budgetService.setBudget(currentUserId(jwt), category, request.monthlyLimit());
    }

    @DeleteMapping("/{category}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable Category category, @AuthenticationPrincipal Jwt jwt) {
        budgetService.deleteBudget(currentUserId(jwt), category);
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}