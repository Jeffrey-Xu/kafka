package com.jeffreyxu.kafka.consumer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for serving the consumer web interface
 */
@Controller
public class WebController {

    /**
     * Serve the main consumer dashboard
     */
    @GetMapping("/")
    public String index() {
        return "index.html";
    }
    
    /**
     * Alternative route for consumer dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "index.html";
    }
}
