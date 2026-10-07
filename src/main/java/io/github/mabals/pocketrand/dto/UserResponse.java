package io.github.mabals.pocketrand.dto;

import io.github.mabals.pocketrand.model.User;

public record UserResponse(Long id, String fullName, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail());
    }
}
