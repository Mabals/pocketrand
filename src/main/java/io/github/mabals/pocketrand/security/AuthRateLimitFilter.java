package io.github.mabals.pocketrand.security;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/demo");


    private record Window(Instant start, int count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(@Value("${pocketrand.rate-limit.max-requests:10}") int maxRequests) {
        this.maxRequests = maxRequests;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && LIMITED_PATHS.contains(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr() + "|" + request.getRequestURI();
        Instant now = Instant.now();

        Window window = windows.compute(key, (k, current) ->
                current == null || now.isAfter(current.start().plus(WINDOW))
                        ? new Window(now, 1)
                        : new Window(current.start(), current.count() + 1));

        if (window.count() > maxRequests) {
            long retryAfterSeconds = Duration.between(now, window.start().plus(WINDOW)).toSeconds() + 1;
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"status":429,"error":"Too Many Requests","message":"Too many attempts. Please wait a minute and try again.","fieldErrors":{}}""");
            return;
        }
        chain.doFilter(request, response);
    }

    @Scheduled(fixedRate = 300_000)
    public void removeExpiredWindows() {
        Instant now = Instant.now();
        windows.values().removeIf(window -> now.isAfter(window.start().plus(WINDOW)));
    }

    
}