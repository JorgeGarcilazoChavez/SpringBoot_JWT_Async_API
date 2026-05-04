#!/bin/bash
JAR="$(dirname "$0")/../target/facturacion-0.0.1-SNAPSHOT.jar"
LOG="$(dirname "$0")/../logs/app.log"

mkdir -p "$(dirname "$0")/../logs"

echo "Starting service..."
nohup java -jar "$JAR" > "$LOG" 2>&1 &

echo $! > "$(dirname "$0")/app.pid"
echo "Service started with PID $(cat $(dirname "$0")/app.pid). Log: $LOG"