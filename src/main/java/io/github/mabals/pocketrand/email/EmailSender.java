package io.github.mabals.pocketrand.email;

public interface EmailSender {
    void send(String to, String subject, String body);
}