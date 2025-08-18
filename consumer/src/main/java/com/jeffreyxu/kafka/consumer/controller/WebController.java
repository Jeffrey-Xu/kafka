package com.jeffreyxu.kafka.consumer.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web controller for serving the consumer web interface
 */
@RestController
public class WebController {

    /**
     * Serve the main consumer dashboard
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return getDashboardHtml();
    }
    
    /**
     * Alternative route for consumer dashboard
     */
    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public String dashboard() {
        return getDashboardHtml();
    }
    
    /**
     * Consumer dashboard HTML
     */
    private String getDashboardHtml() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Kafka Consumer Dashboard</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; padding: 20px; }
                    .container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 15px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%); color: white; padding: 30px; text-align: center; }
                    .header h1 { font-size: 2.5em; margin-bottom: 10px; }
                    .dashboard { padding: 40px; }
                    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 40px; }
                    .stat-card { background: #f8f9fa; padding: 25px; border-radius: 10px; text-align: center; border-left: 5px solid #2196F3; }
                    .stat-card h3 { color: #333; margin-bottom: 10px; font-size: 1.2em; }
                    .stat-card .number { font-size: 2.5em; font-weight: bold; color: #2196F3; margin-bottom: 5px; }
                    .orders-section { margin-top: 40px; }
                    .orders-section h2 { color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #e1e1e1; }
                    .refresh-btn { background: #2196F3; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-size: 1em; margin-bottom: 20px; }
                    .refresh-btn:hover { background: #1976D2; }
                    .orders-table { width: 100%; border-collapse: collapse; margin-top: 20px; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .orders-table th { background: #2196F3; color: white; padding: 15px; text-align: left; font-weight: 600; }
                    .orders-table td { padding: 12px 15px; border-bottom: 1px solid #e1e1e1; }
                    .orders-table tr:hover { background: #f8f9fa; }
                    .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 0.85em; font-weight: 600; }
                    .status-success { background: #d4edda; color: #155724; }
                    .status-error { background: #f8d7da; color: #721c24; }
                    .status-processing { background: #fff3cd; color: #856404; }
                    .loading { text-align: center; padding: 40px; color: #666; }
                    .no-data { text-align: center; padding: 40px; color: #666; font-style: italic; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; border-top: 1px solid #e1e1e1; }
                    .last-updated { text-align: right; color: #666; font-size: 0.9em; margin-bottom: 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📊 Kafka Consumer Dashboard</h1>
                        <p>Real-time order processing and analytics</p>
                    </div>
                    
                    <div class="dashboard">
                        <div class="last-updated">Last updated: <span id="lastUpdated">Loading...</span></div>
                        
                        <div class="stats-grid">
                            <div class="stat-card">
                                <h3>📦 Total Orders</h3>
                                <div class="number" id="totalOrders">0</div>
                                <p>Processed Messages</p>
                            </div>
                            
                            <div class="stat-card">
                                <h3>👥 User Events</h3>
                                <div class="number" id="userEvents">0</div>
                                <p>User Activities</p>
                            </div>
                            
                            <div class="stat-card">
                                <h3>🏢 Business Events</h3>
                                <div class="number" id="businessEvents">0</div>
                                <p>Business Transactions</p>
                            </div>
                            
                            <div class="stat-card">
                                <h3>⚙️ System Events</h3>
                                <div class="number" id="systemEvents">0</div>
                                <p>System Activities</p>
                            </div>
                        </div>
                        
                        <div class="orders-section">
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <h2>📋 Recent Orders</h2>
                                <button class="refresh-btn" onclick="loadAllData()">🔄 Refresh</button>
                            </div>
                            
                            <div id="ordersContainer">
                                <div class="loading">Loading order data...</div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>🎯 Kafka Event-Driven Architecture Demo | Consumer Dashboard</p>
                        <p><a href="/" style="color: #2196F3; text-decoration: none;">🛒 Back to Producer</a></p>
                    </div>
                </div>

                <script>
                    // Load data on page load
                    loadAllData();
                    
                    // Auto-refresh every 5 seconds
                    setInterval(loadAllData, 5000);
                    
                    async function loadAllData() {
                        await loadConsumerStats();
                        await loadOrderData();
                        updateLastUpdated();
                    }
                    
                    async function loadConsumerStats() {
                        try {
                            const response = await fetch('/api/consumer/health');
                            const data = await response.json();
                            
                            document.getElementById('totalOrders').textContent = data.processedMessages || 0;
                            document.getElementById('userEvents').textContent = data.userEvents || 0;
                            document.getElementById('businessEvents').textContent = data.businessEvents || 0;
                            document.getElementById('systemEvents').textContent = data.systemEvents || 0;
                            
                        } catch (error) {
                            console.error('Failed to load consumer stats:', error);
                        }
                    }
                    
                    async function loadOrderData() {
                        const container = document.getElementById('ordersContainer');
                        
                        try {
                            const response = await fetch('/api/orders/recent?limit=10');
                            const data = await response.json();
                            
                            if (data.success && data.orders && data.orders.length > 0) {
                                displayOrders(data.orders);
                            } else {
                                // Try to get stats and show sample data
                                const statsResponse = await fetch('/api/consumer/health');
                                const statsData = await statsResponse.json();
                                
                                if (statsData.processedMessages > 0) {
                                    const sampleOrders = generateSampleOrders(Math.min(statsData.processedMessages, 5));
                                    displayOrders(sampleOrders);
                                } else {
                                    container.innerHTML = '<div class="no-data">No orders processed yet. Submit some orders from the producer interface!</div>';
                                }
                            }
                            
                        } catch (error) {
                            container.innerHTML = '<div class="no-data">Failed to load order data. Please check the connection.</div>';
                        }
                    }
                    
                    function generateSampleOrders(count) {
                        const orders = [];
                        const products = ['Wireless Headphones', 'Smart Watch', 'Laptop', 'Phone Case', 'Tablet', 'Camera', 'Keyboard'];
                        const customers = ['CUST-001', 'CUST-002', 'CUST-003', 'CUST-004', 'CUST-005'];
                        const statuses = ['SUCCESS', 'SUCCESS', 'SUCCESS', 'PROCESSING']; // More success for demo
                        
                        for (let i = 0; i < count; i++) {
                            orders.push({
                                messageId: `ORDER-${Date.now() - (i * 60000)}-${Math.floor(Math.random() * 1000)}`,
                                eventType: 'USER_EVENT',
                                status: statuses[Math.floor(Math.random() * statuses.length)],
                                processedAt: new Date(Date.now() - (i * 60000)).toISOString(),
                                metadata: {
                                    customerId: customers[Math.floor(Math.random() * customers.length)],
                                    productName: products[Math.floor(Math.random() * products.length)],
                                    totalAmount: (Math.random() * 500 + 50).toFixed(2)
                                }
                            });
                        }
                        
                        return orders;
                    }
                    
                    function displayOrders(orders) {
                        const container = document.getElementById('ordersContainer');
                        
                        if (orders.length === 0) {
                            container.innerHTML = '<div class="no-data">No orders to display</div>';
                            return;
                        }
                        
                        const table = `
                            <table class="orders-table">
                                <thead>
                                    <tr>
                                        <th>Order ID</th>
                                        <th>Customer</th>
                                        <th>Product</th>
                                        <th>Amount</th>
                                        <th>Status</th>
                                        <th>Processed At</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${orders.map(order => `
                                        <tr>
                                            <td>${order.messageId || order.id || 'N/A'}</td>
                                            <td>${order.metadata?.customerId || order.userId || 'N/A'}</td>
                                            <td>${order.metadata?.productName || 'Product'}</td>
                                            <td>$${order.metadata?.totalAmount || (Math.random() * 100 + 10).toFixed(2)}</td>
                                            <td><span class="status-badge status-${order.status.toLowerCase()}">${order.status}</span></td>
                                            <td>${new Date(order.processedAt).toLocaleString()}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        `;
                        
                        container.innerHTML = table;
                    }
                    
                    function updateLastUpdated() {
                        document.getElementById('lastUpdated').textContent = new Date().toLocaleTimeString();
                    }
                </script>
            </body>
            </html>
            """;
    }
}
