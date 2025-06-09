DEFAULT_IP="127.0.0.1"
DEFAULT_PORT="12345"


start_server() {
    local port="${1:-$DEFAULT_PORT}"
    echo "Starting Domino Server on port $port..."
    java -jar target/domino-server-jar-with-dependencies.jar -i $port &
    SERVER_PID=$!
    sleep 2  # Give server time to start
}

start_client() {
    local ip="${1:-$DEFAULT_IP}"
    local port="${2:-$DEFAULT_PORT}"
    echo "Starting Domino Client, connecting to $ip:$port..."
    java -jar target/domino-client-jar-with-dependencies.jar -i $ip $port
}

stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        echo "Stopping server..."
        kill $SERVER_PID
    fi
}

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
    *)
        echo "Usage:"
        echo "  $0 server [port]               # Start server (default port: $DEFAULT_PORT)"
        echo "  $0 client [ip] [port]          # Start client (default: $DEFAULT_IP:$DEFAULT_PORT)"
        echo "  $0 test [port]                 # Start both server and client for testing"
        exit 1
        ;;
esac