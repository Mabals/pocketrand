package io.github.mabals.pocketrand.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "PocketRand", LocalDateTime.now().toString());
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "there") String name) {
        return "Hello " + name + ", welcome to PocketRand!";
    }

    public record HealthResponse(String status, String app, String checkedAt) {
    }
}
