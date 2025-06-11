package server;

import java.io.*;
import java.net.*;
import java.util.*;

import connection.Connection;

public class DominoServer {
    private final static int port = 0;

    private static SessionManager manager = new SessionManager();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Domino Server started on port " + port);

            while (!serverSocket.isClosed()) {
                Connection newPlayer = new Connection(serverSocket.accept());
                // start thread on new connection
                manager.startNewClient(newPlayer);
            }
        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
        } finally {
            manager.closeAllSessions();
        }
    }
}
