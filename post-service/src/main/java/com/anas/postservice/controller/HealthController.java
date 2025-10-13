package com.anas.postservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/post/health")
@Tag(name = "Health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "post-service");
        health.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> details = new HashMap<>();
        details.put("version", "1.0.0");
        details.put("description", "Post Service for Chat Application");
        
        health.put("details", details);
        
        return ResponseEntity.ok(health);
    }
}