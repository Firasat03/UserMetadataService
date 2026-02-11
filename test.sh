#!/bin/bash

BASE_URL="http://localhost:8080/user"

echo "1. Testing POST $BASE_URL (Create User)..."
USER_JSON='{
    "name": "Test User",
    "email": "test.user@example.com",
    "phone": "1234567890"
}'

# Use curl to perform the POST request
RESPONSE=$(curl -s -X POST "$BASE_URL" \
     -H "Content-Type: application/json" \
     -d "$USER_JSON")

if [ $? -eq 0 ]; then
    echo -e "\e[32mCreate Response: $RESPONSE\e[0m"
else
    echo "Failed to create user." >&2
    exit 1
fi

# Pause briefly
sleep 1

echo -e "\n2. Testing GET $BASE_URL/1 (Get User with ID 1)..."
# Use curl to perform the GET request
GET_RESPONSE=$(curl -s -X GET "$BASE_URL/1")

if [ $? -eq 0 ]; then
    echo -e "\e[32mGet Response:\e[0m"
    echo "$GET_RESPONSE" | python3 -m json.tool || echo "$GET_RESPONSE"
else
    echo "Failed to get user." >&2
fi
