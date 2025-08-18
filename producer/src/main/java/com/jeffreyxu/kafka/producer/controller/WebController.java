package com.jeffreyxu.kafka.producer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for serving the producer web interface
 */
@Controller
public class WebController {

    /**
     * Serve the main producer web interface
     */
    @GetMapping("/")
    public String index() {
        return "index.html";
    }
    
    /**
     * Redirect to consumer dashboard
     */
    @GetMapping("/consumer")
    public String consumer() {
        return "redirect:http://kafka-demo.ciscloudlab.link/api/consumer/";
    }
}
