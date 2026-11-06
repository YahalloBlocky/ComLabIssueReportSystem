package edu.cvsu.labissues.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cvsu.labissues.repository.HealthRepository;

@RestController
public class HealthController {

    private final HealthRepository health;

    public HealthController(HealthRepository health) {
        this.health = health;
    }

    /** Open http://localhost:8080/api/health to check that the app can reach MySQL. */
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "database", health.ping() ? "UP" : "DOWN");
    }
}
