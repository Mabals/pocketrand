package io.github.mabals.pocketrand.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.email.EmailSender;
import io.github.mabals.pocketrand.model.PasswordResetToken;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.PasswordResetTokenRepository;
import io.github.mabals.pocketrand.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String INVALID_LINK = "This reset link is invalid or has expired. Please request a new one.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final String frontendUrl;
    private final Duration expiry;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailSender emailSender,
                                @Value("${pocketrand.frontend-url}") String frontendUrl,
                                @Value("${pocketrand.password-reset.expiry-minutes}") long expiryMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.frontendUrl = frontendUrl;
        this.expiry = Duration.ofMinutes(expiryMinutes);
    }

    @Transactional
    public void requestReset(String email) {
        String normalisedEmail = email.trim().toLowerCase();
        userRepository.findByEmail(normalisedEmail).ifPresent(this::createAndSendToken);
        // Deliberately no "else": the caller can't tell whether the email exists
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(sha256(token))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LINK));
        if (resetToken.isExpired(Instant.now())) {
            throw new IllegalArgumentException(INVALID_LINK);
        }

        User user = resetToken.getUser();
        user.changePasswordHash(passwordEncoder.encode(newPassword));
        tokenRepository.deleteAllByUserId(user.getId());   // single use: every link for this user stops working
    }

    private void createAndSendToken(User user) {
        tokenRepository.deleteAllByUserId(user.getId());   // only the newest link works

        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        tokenRepository.save(new PasswordResetToken(user, sha256(token), Instant.now().plus(expiry)));

        String link = frontendUrl + "/reset-password?token=" + token;
        String firstName = user.getFullName().split(" ")[0];
        emailSender.send(user.getEmail(), "Reset your PocketRand password", """
                Hi %s,

                Someone asked to reset your PocketRand password. Use this link within %d minutes:
                %s

                If this wasn't you, you can ignore this email. Your password won't change.
                """.formatted(firstName, expiry.toMinutes(), link));
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}