package io.github.mabals.pocketrand.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.mabals.pocketrand.dto.RegisterRequest;
import io.github.mabals.pocketrand.dto.UserResponse;
import io.github.mabals.pocketrand.exception.EmailAlreadyUsedException;
import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = userRepository.save(new User(request.fullName().trim(), email, passwordHash));
        return UserResponse.from(user);
    }
}
