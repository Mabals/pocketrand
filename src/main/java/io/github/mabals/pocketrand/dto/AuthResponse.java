package io.github.mabals.pocketrand.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {
}
