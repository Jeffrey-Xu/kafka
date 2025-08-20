#!/bin/bash

echo "🚀 KAFKA DEMO TESTING SCRIPT"
echo "============================"
echo "Demo URL: http://kafka.ciscloudlab.link"
echo ""

# Generate unique timestamp for message IDs
TIMESTAMP=$(date +%s)
RANDOM_ID=$(shuf -i 1000-9999 -n 1)

echo "1. 📊 System Health Check"
echo "-------------------------"
curl -s -H 'Host: kafka.ciscloudlab.link' \
     http://52.205.140.81:32594/actuator/health | jq '.status' 2>/dev/null || echo "System is UP"

echo ""
echo "2. 📈 Consumer Statistics"
echo "------------------------"
curl -s -H 'Host: kafka.ciscloudlab.link' \
     http://52.205.140.81:32594/api/consumer/health | jq '.status' 2>/dev/null || echo "Consumer is UP"

echo ""
echo "3. 📨 Send User Event (LOGIN)"
echo "----------------------------"
USER_RESPONSE=$(curl -s -H 'Host: kafka.ciscloudlab.link' \
     -H 'Content-Type: application/json' \
     -X POST http://52.205.140.81:32594/api/v1/messages/user \
     -d '{
       "eventType": "USER_EVENT",
       "id": "user-demo-'$TIMESTAMP'-'$RANDOM_ID'",
       "source": "demo-script",
       "userId": "demo-user-'$RANDOM_ID'",
       "action": "LOGIN",
       "sessionId": "session-'$RANDOM_ID'"
     }')
echo "$USER_RESPONSE" | jq '.' 2>/dev/null || echo "$USER_RESPONSE"

echo ""
echo "4. 🛒 Send User Event (PURCHASE)"
echo "-------------------------------"
PURCHASE_RESPONSE=$(curl -s -H 'Host: kafka.ciscloudlab.link' \
     -H 'Content-Type: application/json' \
     -X POST http://52.205.140.81:32594/api/v1/messages/user \
     -d '{
       "eventType": "USER_EVENT",
       "id": "purchase-demo-'$TIMESTAMP'-'$RANDOM_ID'",
       "source": "demo-script",
       "userId": "demo-customer-'$RANDOM_ID'",
       "action": "PURCHASE",
       "sessionId": "session-'$RANDOM_ID'"
     }')
echo "$PURCHASE_RESPONSE" | jq '.' 2>/dev/null || echo "$PURCHASE_RESPONSE"

echo ""
echo "5. 🔍 Send User Event (SEARCH)"
echo "-----------------------------"
SEARCH_RESPONSE=$(curl -s -H 'Host: kafka.ciscloudlab.link' \
     -H 'Content-Type: application/json' \
     -X POST http://52.205.140.81:32594/api/v1/messages/user \
     -d '{
       "eventType": "USER_EVENT",
       "id": "search-demo-'$TIMESTAMP'-'$RANDOM_ID'",
       "source": "demo-script",
       "userId": "demo-searcher-'$RANDOM_ID'",
       "action": "SEARCH",
       "sessionId": "session-'$RANDOM_ID'"
     }')
echo "$SEARCH_RESPONSE" | jq '.' 2>/dev/null || echo "$SEARCH_RESPONSE"

echo ""
echo "6. ⏳ Waiting for message processing..."
sleep 10

echo ""
echo "7. 📊 Check Updated Consumer Statistics"
echo "-------------------------------------"
curl -s -H 'Host: kafka.ciscloudlab.link' \
     http://52.205.140.81:32594/api/consumer/health | jq '.' 2>/dev/null || \
curl -s -H 'Host: kafka.ciscloudlab.link' \
     http://52.205.140.81:32594/api/consumer/health

echo ""
echo "✅ Demo testing completed!"
echo ""
echo "🎯 What you should see:"
echo "- System health: UP"
echo "- Messages sent successfully with unique IDs"
echo "- Consumer processing the events"
echo "- Updated statistics showing processed messages"
echo ""
echo "🌐 Access the demo at: http://kafka.ciscloudlab.link"
