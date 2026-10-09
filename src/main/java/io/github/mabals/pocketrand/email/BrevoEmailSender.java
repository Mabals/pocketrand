package io.github.mabals.pocketrand.email;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class BrevoEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailSender.class);

    private final RestClient restClient;
    private final Sender sender;

    public BrevoEmailSender(String apiKey, String fromAddress, String fromName) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .requestFactory(requestFactory)
                .build();
        this.sender = new Sender(fromName, fromAddress);
    }

    @Override
    public void send(String to, String subject, String body) {
        BrevoRequest request = new BrevoRequest(sender, List.of(new Recipient(to)), subject, body);
        try {
            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email sent through Brevo: {}", subject);
        } catch (RestClientException e) {
            log.warn("Brevo could not send the email '{}': {}", subject, e.getMessage());
        }
    }

    record Sender(String name, String email) {
    }

    record Recipient(String email) {
    }

    record BrevoRequest(Sender sender, List<Recipient> to, String subject, String textContent) {
    }
}