package com.jeffreyxu.kafka.consumer.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Web controller for serving the consumer web interface
 */
@Controller
public class WebController {

    /**
     * Serve the main consumer dashboard
     */
    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<String> index() {
        try {
            ClassPathResource resource = new ClassPathResource("static/index.html");
            if (resource.exists()) {
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
            } else {
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(getDefaultDashboard());
            }
        } catch (IOException e) {
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(getDefaultDashboard());
        }
    }
    
    /**
     * Alternative route for consumer dashboard
     */
    @GetMapping("/dashboard")
    @ResponseBody
    public ResponseEntity<String> dashboard() {
        return index();
    }
    
    /**
     * Fallback HTML if static file not found
     */
    private String getDefaultDashboard() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Kafka Consumer - Dashboard</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #2196F3; text-align: center; }
                    .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 30px 0; }
                    .stat-card { background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; border-left: 4px solid #2196F3; }
                    .stat-number { font-size: 2em; font-weight: bold; color: #2196F3; }
                    .refresh-btn { background: #2196F3; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; margin-bottom: 20px; }
                    .refresh-btn:hover { background: #1976D2; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
                    th { background: #2196F3; color: white; }
                    .status-success { background: #d4edda; color: #155724; padding: 4px 8px; border-radius: 4px; }
                    .status-error { background: #f8d7da; color: #721c24; padding: 4px 8px; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📊 Kafka Consumer Dashboard</h1>
                    <p>Real-time order processing and analytics</p>
                    
                    <button class="refresh-btn" onclick="loadStats()">🔄 Refresh</button>
                    
                    <div class="stats">
                        <div class="stat-card">
                            <div class="stat-number" id="totalMessages">0</div>
                            <div>Total Messages</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number" id="userEvents">0</div>
                            <div>User Events</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number" id="businessEvents">0</div>
                            <div>Business Events</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number" id="systemEvents">0</div>
                            <div>System Events</div>
                        </div>
                    </div>
                    
                    <h2>📋 Recent Orders</h2>
                    <div id="ordersTable">Loading...</div>
                </div>
                
                <script>
                    async function loadStats() {
                        try {
                            const response = await fetch('/api/consumer/health');
                            const data = await response.json();
                            
                            document.getElementById('totalMessages').textContent = data.processedMessages || 0;
                            document.getElementById('userEvents').textContent = data.userEvents || 0;
                            document.getElementById('businessEvents').textContent = data.businessEvents || 0;
                            document.getElementById('systemEvents').textContent = data.systemEvents || 0;
                            
                        } catch (error) {
                            console.error('Failed to load stats:', error);
                        }
                    }
                    
                    async function loadOrders() {
                        try {
                            const response = await fetch('/api/orders/recent');
                            const data = await response.json();
                            
                            if (data.success && data.orders && data.orders.length > 0) {
                                const table = `
                                    <table>
                                        <thead>
                                            <tr><th>Message ID</th><th>Event Type</th><th>Status</th><th>Processed At</th></tr>
                                        </thead>
                                        <tbody>
                                            ${data.orders.map(order => `
                                                <tr>
                                                    <td>${order.messageId}</td>
                                                    <td>${order.eventType}</td>
                                                    <td><span class="status-${order.status.toLowerCase()}">${order.status}</span></td>
                                                    <td>${new Date(order.processedAt).toLocaleString()}</td>
                                                </tr>
                                            `).join('')}
                                        </tbody>
                                    </table>
                                `;
                                document.getElementById('ordersTable').innerHTML = table;
                            } else {
                                document.getElementById('ordersTable').innerHTML = '<p>No orders processed yet. Submit some orders from the producer!</p>';
                            }
                        } catch (error) {
                            document.getElementById('ordersTable').innerHTML = '<p>Failed to load orders. Check the connection.</p>';
                        }
                    }
                    
                    // Load data on page load
                    loadStats();
                    loadOrders();
                    
                    // Auto-refresh every 10 seconds
                    setInterval(() => {
                        loadStats();
                        loadOrders();
                    }, 10000);
                </script>
            </body>
            </html>
            """;
    }
}
