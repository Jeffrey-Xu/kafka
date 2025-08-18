package com.jeffreyxu.kafka.producer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple test controller to verify Spring Boot is working
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Test endpoint working!";
    }
    
    @GetMapping("/web-test")
    public String webTest() {
        return "<html><body><h1>Web Controller Working!</h1><p>Spring Boot is serving web content.</p></body></html>";
    }
}
