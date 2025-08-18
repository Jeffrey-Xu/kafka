package com.jeffreyxu.kafka.producer.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web controller for serving the producer web interface
 */
@RestController
public class WebController {

    /**
     * Serve the main producer web interface
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Kafka Order Producer</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; padding: 20px; }
                    .container { max-width: 800px; margin: 0 auto; background: white; border-radius: 15px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); color: white; padding: 30px; text-align: center; }
                    .header h1 { font-size: 2.5em; margin-bottom: 10px; }
                    .form-container { padding: 40px; }
                    .form-group { margin-bottom: 25px; }
                    .form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: #333; font-size: 1.1em; }
                    .form-group input, .form-group select { width: 100%; padding: 12px 15px; border: 2px solid #e1e1e1; border-radius: 8px; font-size: 1em; transition: border-color 0.3s ease; }
                    .form-group input:focus, .form-group select:focus { outline: none; border-color: #4CAF50; box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1); }
                    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
                    .btn { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); color: white; padding: 15px 30px; border: none; border-radius: 8px; font-size: 1.1em; font-weight: 600; cursor: pointer; transition: transform 0.2s ease; width: 100%; }
                    .btn:hover { transform: translateY(-2px); }
                    .result { margin-top: 30px; padding: 20px; border-radius: 8px; display: none; }
                    .result.success { background: #d4edda; border: 1px solid #c3e6cb; color: #155724; }
                    .result.error { background: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; }
                    .stats { background: #f8f9fa; padding: 20px; border-radius: 8px; margin-top: 20px; }
                    .stat-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e1e1e1; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; border-top: 1px solid #e1e1e1; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛒 Kafka Order Producer</h1>
                        <p>Submit orders and watch them flow through the Kafka pipeline</p>
                    </div>
                    
                    <div class="form-container">
                        <form id="orderForm">
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="customerId">Customer ID</label>
                                    <input type="text" id="customerId" name="customerId" required>
                                </div>
                                <div class="form-group">
                                    <label for="customerEmail">Customer Email</label>
                                    <input type="email" id="customerEmail" name="customerEmail" required placeholder="customer@example.com">
                                </div>
                            </div>
                            
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="productName">Product Name</label>
                                    <input type="text" id="productName" name="productName" required placeholder="e.g., Wireless Headphones">
                                </div>
                                <div class="form-group">
                                    <label for="productCategory">Category</label>
                                    <select id="productCategory" name="productCategory" required>
                                        <option value="">Select Category</option>
                                        <option value="ELECTRONICS">Electronics</option>
                                        <option value="CLOTHING">Clothing</option>
                                        <option value="BOOKS">Books</option>
                                        <option value="HOME">Home & Garden</option>
                                        <option value="SPORTS">Sports</option>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="quantity">Quantity</label>
                                    <input type="number" id="quantity" name="quantity" required min="1" value="1">
                                </div>
                                <div class="form-group">
                                    <label for="price">Price ($)</label>
                                    <input type="number" id="price" name="price" required min="0" step="0.01" placeholder="99.99">
                                </div>
                            </div>
                            
                            <div class="form-group">
                                <label for="paymentMethod">Payment Method</label>
                                <select id="paymentMethod" name="paymentMethod" required>
                                    <option value="">Select Payment</option>
                                    <option value="CREDIT_CARD">Credit Card</option>
                                    <option value="DEBIT_CARD">Debit Card</option>
                                    <option value="PAYPAL">PayPal</option>
                                    <option value="APPLE_PAY">Apple Pay</option>
                                </select>
                            </div>
                            
                            <button type="submit" class="btn">🚀 Submit Order</button>
                        </form>
                        
                        <div class="result" id="result"></div>
                        
                        <div class="stats">
                            <h3>📊 System Status</h3>
                            <div class="stat-item">
                                <span>Producer Status:</span>
                                <span id="producerStatus">Checking...</span>
                            </div>
                            <div class="stat-item">
                                <span>Consumer Status:</span>
                                <span id="consumerStatus">Checking...</span>
                            </div>
                            <div class="stat-item">
                                <span>Orders Submitted:</span>
                                <span id="orderCount">0</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>🎯 Kafka Event-Driven Architecture Demo | Producer Interface</p>
                        <p><a href="/api/consumer/" style="color: #4CAF50; text-decoration: none;">📊 View Consumer Dashboard</a></p>
                    </div>
                </div>

                <script>
                    let orderCounter = parseInt(localStorage.getItem('orderCounter') || '0');
                    document.getElementById('orderCount').textContent = orderCounter;
                    
                    // Auto-generate customer ID
                    document.getElementById('customerId').value = 'CUST-' + Date.now();
                    
                    // Check system status on load
                    checkSystemStatus();
                    
                    document.getElementById('orderForm').addEventListener('submit', async function(e) {
                        e.preventDefault();
                        
                        const result = document.getElementById('result');
                        const form = e.target;
                        
                        // Collect form data
                        const formData = new FormData(form);
                        const orderData = {
                            eventType: "USER_EVENT",
                            id: "ORDER-" + Date.now() + "-" + Math.floor(Math.random() * 1000),
                            source: "web-interface",
                            userId: formData.get('customerId'),
                            action: "PURCHASE",
                            sessionId: "WEB-" + Date.now(),
                            metadata: {
                                customerEmail: formData.get('customerEmail'),
                                productName: formData.get('productName'),
                                productCategory: formData.get('productCategory'),
                                quantity: parseInt(formData.get('quantity')),
                                price: parseFloat(formData.get('price')),
                                totalAmount: parseInt(formData.get('quantity')) * parseFloat(formData.get('price')),
                                paymentMethod: formData.get('paymentMethod'),
                                orderTimestamp: new Date().toISOString()
                            }
                        };
                        
                        try {
                            const response = await fetch('/api/v1/messages/user', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                },
                                body: JSON.stringify(orderData)
                            });
                            
                            const responseData = await response.json();
                            
                            result.style.display = 'block';
                            
                            if (response.ok && responseData.success) {
                                result.className = 'result success';
                                result.innerHTML = `
                                    <h3>✅ Order Submitted Successfully!</h3>
                                    <p><strong>Order ID:</strong> ${responseData.messageId}</p>
                                    <p><strong>Description:</strong> ${responseData.description}</p>
                                    <p><strong>Total Amount:</strong> $${orderData.metadata.totalAmount.toFixed(2)}</p>
                                    <p>Your order has been sent to the Kafka pipeline for processing.</p>
                                `;
                                
                                // Increment counter
                                orderCounter++;
                                localStorage.setItem('orderCounter', orderCounter.toString());
                                document.getElementById('orderCount').textContent = orderCounter;
                                
                                // Reset form
                                form.reset();
                                document.getElementById('customerId').value = 'CUST-' + Date.now();
                                
                            } else {
                                throw new Error(responseData.message || 'Failed to submit order');
                            }
                            
                        } catch (error) {
                            result.style.display = 'block';
                            result.className = 'result error';
                            result.innerHTML = `
                                <h3>❌ Order Submission Failed</h3>
                                <p><strong>Error:</strong> ${error.message}</p>
                                <p>Please check your input and try again.</p>
                            `;
                        }
                    });
                    
                    async function checkSystemStatus() {
                        try {
                            // Check producer status
                            const producerResponse = await fetch('/actuator/health');
                            const producerData = await producerResponse.json();
                            document.getElementById('producerStatus').textContent = 
                                producerData.status === 'UP' ? '🟢 Online' : '🔴 Offline';
                            
                            // Check consumer status
                            const consumerResponse = await fetch('/api/consumer/health');
                            const consumerData = await consumerResponse.json();
                            document.getElementById('consumerStatus').textContent = 
                                consumerData.status === 'UP' ? '🟢 Online' : '🔴 Offline';
                                
                        } catch (error) {
                            document.getElementById('producerStatus').textContent = '🔴 Error';
                            document.getElementById('consumerStatus').textContent = '🔴 Error';
                        }
                    }
                    
                    // Refresh status every 30 seconds
                    setInterval(checkSystemStatus, 30000);
                </script>
            </body>
            </html>
            """;
    }
}
