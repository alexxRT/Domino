package server;

import java.io.*;
import java.net.*;
import java.util.*;

import connection.Connection;

public class DominoServer {
    private static int port = 12345;

    private static SessionManager manager = new SessionManager();

    public static void main(String[] args) {
        String[] posArgs = {"server", "port"};

        if (args.length != posArgs.length) {
            System.out.println("Start server with positional arguments! Usage example:");
            System.out.println(posArgs[0] + " -i <" + posArgs[1] + ">");
            return;
        }

        port = Integer.parseInt(args[1]);

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
