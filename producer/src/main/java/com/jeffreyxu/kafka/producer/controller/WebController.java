package com.jeffreyxu.kafka.producer.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Web controller for serving the producer web interface
 */
@Controller
public class WebController {

    /**
     * Serve the main producer web interface
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
                    .body(getDefaultHtml());
            }
        } catch (IOException e) {
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(getDefaultHtml());
        }
    }
    
    /**
     * Fallback HTML if static file not found
     */
    private String getDefaultHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Kafka Producer - Demo Interface</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #4CAF50; text-align: center; }
                    .form-group { margin: 20px 0; }
                    label { display: block; margin-bottom: 5px; font-weight: bold; }
                    input, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
                    button { background: #4CAF50; color: white; padding: 12px 24px; border: none; border-radius: 5px; cursor: pointer; width: 100%; font-size: 16px; }
                    button:hover { background: #45a049; }
                    .result { margin-top: 20px; padding: 15px; border-radius: 5px; }
                    .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
                    .error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🛒 Kafka Producer Demo</h1>
                    <p>Submit orders to test the Kafka event-driven system</p>
                    
                    <form id="orderForm">
                        <div class="form-group">
                            <label>Customer ID:</label>
                            <input type="text" id="customerId" required>
                        </div>
                        <div class="form-group">
                            <label>Product Name:</label>
                            <input type="text" id="productName" placeholder="e.g., Wireless Headphones" required>
                        </div>
                        <div class="form-group">
                            <label>Price ($):</label>
                            <input type="number" id="price" step="0.01" placeholder="99.99" required>
                        </div>
                        <div class="form-group">
                            <label>Action:</label>
                            <select id="action" required>
                                <option value="PURCHASE">Purchase</option>
                                <option value="LOGIN">Login</option>
                                <option value="SEARCH">Search</option>
                                <option value="BROWSE">Browse</option>
                            </select>
                        </div>
                        <button type="submit">🚀 Submit Order</button>
                    </form>
                    
                    <div id="result" style="display:none;"></div>
                </div>
                
                <script>
                    document.getElementById('customerId').value = 'CUST-' + Date.now();
                    
                    document.getElementById('orderForm').addEventListener('submit', async function(e) {
                        e.preventDefault();
                        
                        const orderData = {
                            eventType: "USER_EVENT",
                            id: "ORDER-" + Date.now(),
                            source: "web-demo",
                            userId: document.getElementById('customerId').value,
                            action: document.getElementById('action').value,
                            sessionId: "WEB-" + Date.now(),
                            metadata: {
                                productName: document.getElementById('productName').value,
                                price: parseFloat(document.getElementById('price').value)
                            }
                        };
                        
                        try {
                            const response = await fetch('/api/v1/messages/user', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(orderData)
                            });
                            
                            const result = await response.json();
                            const resultDiv = document.getElementById('result');
                            resultDiv.style.display = 'block';
                            
                            if (response.ok && result.success) {
                                resultDiv.className = 'result success';
                                resultDiv.innerHTML = `<h3>✅ Order Submitted!</h3><p>Order ID: ${result.messageId}</p>`;
                                document.getElementById('orderForm').reset();
                                document.getElementById('customerId').value = 'CUST-' + Date.now();
                            } else {
                                throw new Error(result.message || 'Failed to submit order');
                            }
                        } catch (error) {
                            const resultDiv = document.getElementById('result');
                            resultDiv.style.display = 'block';
                            resultDiv.className = 'result error';
                            resultDiv.innerHTML = `<h3>❌ Error</h3><p>${error.message}</p>`;
                        }
                    });
                </script>
            </body>
            </html>
            """;
    }
}
