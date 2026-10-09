package io.github.mabals.pocketrand.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("""

                ----- EMAIL (development only, not actually sent) -----
                To: {}
                Subject: {}

                {}
                -------------------------------------------------------""", to, subject, body);
    }
}