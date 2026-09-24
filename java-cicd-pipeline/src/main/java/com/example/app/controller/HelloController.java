package com.example.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "devops-demo-app");
        body.put("message", "CI/CD pipeline is working!");
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @GetMapping("/api/hello")
    public Map<String, String> hello(@RequestParam(defaultValue = "World") String name) {
        return Map.of("greeting", "Hello, " + name + "!");
    }

    @GetMapping("/api/version")
    public Map<String, String> version() {
        return Map.of("version", "1.0.0", "build", "jenkins-ci");
    }
}
