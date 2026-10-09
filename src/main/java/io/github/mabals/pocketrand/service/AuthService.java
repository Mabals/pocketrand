package io.github.mabals.pocketrand.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.dto.AuthResponse;
import io.github.mabals.pocketrand.dto.LoginRequest;
import io.github.mabals.pocketrand.dto.RegisterRequest;
import io.github.mabals.pocketrand.dto.UserResponse;
import io.github.mabals.pocketrand.exception.EmailAlreadyUsedException;
import io.github.mabals.pocketrand.exception.InvalidCredentialsException;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.UserRepository;
import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final DemoDataService demoDataService;                     // ← 2. new field

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       DemoDataService demoDataService) {              // ← 3. new parameter
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.demoDataService = demoDataService;                        // ← 4. new assignment
    }

    // ... register, login, getCurrentUser stay exactly as they are ...

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(request.fullName().trim(), email, passwordHash);
        user.recordPrivacyConsent(Instant.now());
        userRepository.save(user);
        return UserResponse.from(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenService.createToken(user);
        return new AuthResponse(token, "Bearer", tokenService.getExpirySeconds(), UserResponse.from(user));
    }

    public UserResponse getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(InvalidCredentialsException::new);
    }

    @Transactional
    public AuthResponse loginAsDemo() {
        User demoUser = demoDataService.createDemoUser();
        String token = tokenService.createToken(demoUser);
        return new AuthResponse(token, "Bearer", tokenService.getExpirySeconds(), UserResponse.from(demoUser));
    }
}
