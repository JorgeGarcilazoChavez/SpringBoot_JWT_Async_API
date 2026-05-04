#!/bin/bash
PID_FILE="$(dirname "$0")/app.pid"

echo "Stopping service on port 8080..."
PID=$(netstat -ano | grep ":8080" | grep "LISTENING" | awk '{print $5}')

if [ -z "$PID" ]; then
    echo "No process found on port 8080."
else
    taskkill //F //PID $PID
    echo "Service with PID $PID stopped."
fi

if [ -f "$PID_FILE" ]; then
    rm "$PID_FILE"
fi