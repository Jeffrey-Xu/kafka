package com.jeffreyxu.kafka.consumer.controller;

import com.jeffreyxu.kafka.consumer.entity.ProcessedMessage;
import com.jeffreyxu.kafka.consumer.repository.ProcessedMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for orders and processed messages data
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrdersController {

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    /**
     * Get recent processed orders/messages
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            // Get recent processed messages
            PageRequest pageRequest = PageRequest.of(0, limit, 
                Sort.by(Sort.Direction.DESC, "processedAt"));
            
            List<ProcessedMessage> recentMessages = processedMessageRepository
                .findAll(pageRequest).getContent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", recentMessages);
            response.put("total", processedMessageRepository.count());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch orders: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get order statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        
        try {
            long totalOrders = processedMessageRepository.count();
            long successfulOrders = processedMessageRepository.countByStatus("SUCCESS");
            long failedOrders = processedMessageRepository.countByStatus("FAILED");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", totalOrders);
            stats.put("successfulOrders", successfulOrders);
            stats.put("failedOrders", failedOrders);
            stats.put("successRate", totalOrders > 0 ? (double) successfulOrders / totalOrders * 100 : 0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch stats: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get orders by status
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            PageRequest pageRequest = PageRequest.of(0, limit, 
                Sort.by(Sort.Direction.DESC, "processedAt"));
            
            List<ProcessedMessage> messages = processedMessageRepository
                .findByStatusOrderByProcessedAtDesc(status, pageRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", messages);
            response.put("status", status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch orders by status: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
