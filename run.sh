#!/usr/bin/env bash

DEFAULT_IP="127.0.0.1"
DEFAULT_PORT="12345"

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        echo "Java not found. Installing OpenJDK..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            brew install openjdk@11
            sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            # Linux
            sudo apt-get update
            sudo apt-get install -y openjdk-11-jdk
        else
            echo "Unsupported operating system"
            exit 1
        fi
    fi
}

# Check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null; then
        echo "Maven not found. Installing Maven..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            brew install maven
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            # Linux
            sudo apt-get update
            sudo apt-get install -y maven
        else
            echo "Unsupported operating system"
            exit 1
        fi
    fi
}

# Check requirements
check_java
check_maven

# Build the project
mvn clean package

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Build failed"
    exit 1
fi

# Function to start server
start_server() {
    local port="${1:-$DEFAULT_PORT}"
    echo "Starting Domino Server on port $port..."
    java -jar target/domino-server-jar-with-dependencies.jar -i $port &
    SERVER_PID=$!
    sleep 2  # Give server time to start
}

# Function to start client
start_client() {
    local ip="${1:-$DEFAULT_IP}"
    local port="${2:-$DEFAULT_PORT}"
    echo "Starting Domino Client, connecting to $ip:$port..."
    java -jar target/domino-client-jar-with-dependencies.jar -i $ip $port
}

# Function to stop server
stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        echo "Stopping server..."
        kill $SERVER_PID
    fi
}

# Setup trap to ensure server is stopped when script exits
trap stop_server EXIT

case "$1" in
    "server")
        port="${2:-$DEFAULT_PORT}"
        start_server "$port"
        wait $SERVER_PID
        ;;
    "client")
        ip="${2:-$DEFAULT_IP}"
        port="${3:-$DEFAULT_PORT}"
        start_client "$ip" "$port"
        ;;
    "test")
        port="${2:-$DEFAULT_PORT}"
        start_server "$port"
        start_client "$DEFAULT_IP" "$port"
        ;;
    *)
        echo "Usage:"
        echo "  $0 server [port]               # Start server (default port: $DEFAULT_PORT)"
        echo "  $0 client [ip] [port]          # Start client (default: $DEFAULT_IP:$DEFAULT_PORT)"
        echo "  $0 test [port]                 # Start both server and client for testing"
        exit 1
        ;;
esac
