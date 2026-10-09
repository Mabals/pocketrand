package io.github.mabals.pocketrand.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.mabals.pocketrand.email.BrevoEmailSender;
import io.github.mabals.pocketrand.email.EmailSender;
import io.github.mabals.pocketrand.email.LoggingEmailSender;

@Configuration
public class EmailConfig {

    private static final Logger log = LoggerFactory.getLogger(EmailConfig.class);

    @Bean
    public EmailSender emailSender(
            @Value("${brevo.api-key:}") String apiKey,
            @Value("${email.from-address:}") String fromAddress,
            @Value("${email.from-name:PocketRand}") String fromName) {

        if (apiKey.isBlank()) {
            log.info("No BREVO_API_KEY set: emails will be printed to the log, not sent");
            return new LoggingEmailSender();
        }
        if (fromAddress.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY is set, but EMAIL_FROM is missing");
        }
        log.info("Emails will be sent through Brevo");
        return new BrevoEmailSender(apiKey, fromAddress, fromName);
    }
}