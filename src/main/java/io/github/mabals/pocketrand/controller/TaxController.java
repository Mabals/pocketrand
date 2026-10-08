package io.github.mabals.pocketrand.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.mabals.pocketrand.dto.TaxEstimateRequest;
import io.github.mabals.pocketrand.dto.TaxEstimateResponse;
import io.github.mabals.pocketrand.service.TaxService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tax")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @PostMapping("/estimate")
    public TaxEstimateResponse estimate(@Valid @RequestBody TaxEstimateRequest request) {
        return taxService.estimate(request);
    }
}