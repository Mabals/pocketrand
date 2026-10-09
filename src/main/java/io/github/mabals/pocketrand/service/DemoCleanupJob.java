package io.github.mabals.pocketrand.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.mabals.pocketrand.model.User;
import io.github.mabals.pocketrand.repository.UserRepository;

@Component
public class DemoCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(DemoCleanupJob.class);
    private static final Duration DEMO_LIFETIME = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final AccountService accountService;

    public DemoCleanupJob(UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void removeOldDemoAccounts() {
        Instant cutoff = Instant.now().minus(DEMO_LIFETIME);
        List<User> oldDemoUsers = userRepository.findAllByEmailEndingWithAndCreatedAtBefore(
                DemoDataService.DEMO_EMAIL_DOMAIN, cutoff);

        oldDemoUsers.forEach(user -> accountService.deleteUserAndData(user.getId()));

        if (!oldDemoUsers.isEmpty()) {
            log.info("Removed {} demo accounts older than 24 hours", oldDemoUsers.size());
        }
    }
}