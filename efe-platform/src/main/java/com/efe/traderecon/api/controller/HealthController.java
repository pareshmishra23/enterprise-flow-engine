package com.efe.traderecon.api.controller;

import com.efe.traderecon.ikasan.engine.IkasanEngine;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    private final IkasanEngine ikasanEngine;

    public HealthController(IkasanEngine ikasanEngine) {
        this.ikasanEngine = ikasanEngine;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        boolean isModuleRunning = ikasanEngine.getModule() != null && ikasanEngine.getModule().isRunning();
        if (isModuleRunning) {
            return ResponseEntity.ok(Map.of(
                    "status", "READY",
                    "timestamp", Instant.now().toString(),
                    "module", ikasanEngine.getModule().getName(),
                    "activeFlows", ikasanEngine.getModule().getFlows().size()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "NOT_READY",
                    "timestamp", Instant.now().toString(),
                    "reason", "Ikasan module is not running"
            ));
        }
    }
}
