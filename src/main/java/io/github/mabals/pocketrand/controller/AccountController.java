package io.github.mabals.pocketrand.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.mabals.pocketrand.dto.ChangePasswordRequest;
import io.github.mabals.pocketrand.dto.DeleteAccountRequest;
import io.github.mabals.pocketrand.service.AccountService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request,
                               @AuthenticationPrincipal Jwt jwt) {
        accountService.changePassword(Long.valueOf(jwt.getSubject()), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@Valid @RequestBody DeleteAccountRequest request,
                              @AuthenticationPrincipal Jwt jwt) {
        accountService.deleteAccount(Long.valueOf(jwt.getSubject()), request);
    }
}