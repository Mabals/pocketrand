package io.github.mabals.pocketrand.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Email already in use: " + email);
    }
}