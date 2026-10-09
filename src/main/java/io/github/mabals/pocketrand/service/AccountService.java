package io.github.mabals.pocketrand.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.dto.ChangePasswordRequest;
import io.github.mabals.pocketrand.dto.DeleteAccountRequest;
import io.github.mabals.pocketrand.exception.InvalidCredentialsException;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.BudgetRepository;
import io.github.mabals.pocketrand.repository.PasswordResetTokenRepository;   // NEW
import io.github.mabals.pocketrand.repository.TransactionRepository;
import io.github.mabals.pocketrand.repository.UserRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;   // NEW
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          BudgetRepository budgetRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository,   // NEW
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;   // NEW
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        checkPassword(user, request.currentPassword(), "Current password is incorrect");
        if (request.currentPassword().equals(request.newPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }
        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = getUser(userId);
        checkPassword(user, request.password(), "Password is incorrect");

        // Children first: transactions, budgets and reset tokens point to the user (foreign keys)
        transactionRepository.deleteAllByUserId(userId);
        budgetRepository.deleteAllByUserId(userId);
        passwordResetTokenRepository.deleteAllByUserId(userId);   // NEW
        userRepository.delete(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
    }

    private void checkPassword(User user, String password, String message) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException(message);
        }
    }
}