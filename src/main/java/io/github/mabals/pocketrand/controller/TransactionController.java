package io.github.mabals.pocketrand.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.mabals.pocketrand.dto.ImportResult;
import io.github.mabals.pocketrand.dto.TransactionRequest;
import io.github.mabals.pocketrand.dto.TransactionResponse;
import io.github.mabals.pocketrand.service.StatementImportService;
import io.github.mabals.pocketrand.service.TransactionService;
import io.github.mabals.pocketrand.util.MonthParser;
import jakarta.validation.Valid;



import org.springframework.http.MediaType;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final StatementImportService importService;

    public TransactionController(TransactionService service, StatementImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    public List<TransactionResponse> getAll(@RequestParam(required = false) String month,
                                            @AuthenticationPrincipal Jwt jwt) {
        if (month == null || month.isBlank()) {
            return service.findAll(currentUserId(jwt));
        }
        return service.findForMonth(currentUserId(jwt), MonthParser.parseOrCurrent(month));
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.findById(id, currentUserId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return service.create(request, currentUserId(jwt));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importStatement(@RequestParam("file") MultipartFile file,
                                    @AuthenticationPrincipal Jwt jwt) {
        return importService.importStatement(file, currentUserId(jwt));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return service.update(id, request, currentUserId(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, currentUserId(jwt));
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
