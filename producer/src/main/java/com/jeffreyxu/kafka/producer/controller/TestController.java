package com.jeffreyxu.kafka.producer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple test controller to verify Spring Boot is working
 * Build timestamp: 2025-08-18T15:51:00Z
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Test endpoint working! Build: 2025-08-18T15:51:00Z";
    }
    
    @GetMapping("/web-test")
    public String webTest() {
        return "<html><body><h1>Web Controller Working!</h1><p>Spring Boot is serving web content.</p><p>Build: 2025-08-18T15:51:00Z</p></body></html>";
    }
}
