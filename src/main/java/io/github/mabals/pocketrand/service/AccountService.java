package io.github.mabals.pocketrand.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.dto.ChangePasswordRequest;
import io.github.mabals.pocketrand.dto.DataExport;
import io.github.mabals.pocketrand.dto.DeleteAccountRequest;
import io.github.mabals.pocketrand.dto.TransactionResponse;
import io.github.mabals.pocketrand.exception.InvalidCredentialsException;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.BudgetRepository;
import io.github.mabals.pocketrand.repository.PasswordResetTokenRepository;
import io.github.mabals.pocketrand.repository.TransactionRepository;
import io.github.mabals.pocketrand.repository.UserRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          BudgetRepository budgetRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- Change password ----------

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        checkPassword(user, request.currentPassword(), "Current password is incorrect");
        if (request.currentPassword().equals(request.newPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }
        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    // ---------- Download my data (POPIA: right of access) ----------

    @Transactional(readOnly = true)
    public DataExport exportData(Long userId) {
        User user = getUser(userId);

        List<TransactionResponse> transactions = transactionRepository.findAllByUserIdOrderByDateDescIdDesc(userId)
                .stream()
                .map(TransactionResponse::from)
                .toList();

        List<DataExport.BudgetLimit> budgets = budgetRepository.findAllByUserId(userId)
                .stream()
                .map(budget -> new DataExport.BudgetLimit(budget.getCategory(), budget.getMonthlyLimit()))
                .toList();

        DataExport.Profile profile = new DataExport.Profile(
                user.getFullName(), user.getEmail(), user.getCreatedAt(), user.getPrivacyAcceptedAt());

        return new DataExport(Instant.now(), profile, transactions, budgets);
    }

    // ---------- Delete account (POPIA: right to be forgotten) ----------

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = getUser(userId);
        checkPassword(user, request.password(), "Password is incorrect");
        deleteUserAndData(userId);
    }

    @Transactional
    public void deleteUserAndData(Long userId) {
        // Children first: these tables point to the user (foreign keys)
        passwordResetTokenRepository.deleteAllByUserId(userId);
        transactionRepository.deleteAllByUserId(userId);
        budgetRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    // ---------- Helpers ----------

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
    }

    private void checkPassword(User user, String password, String message) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException(message);
        }
    }
}