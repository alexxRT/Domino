// src/server/DominoServer.java
package server;

import java.io.*;
import java.net.*;
import java.util.*;

import connection.connection;

public class server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(PORT);) {
            System.out.println("Server started!");

            while (!serverSocket.isClosed()) {
                ClientHandler clientHandler = new ClientHandler(new connection(serverSocket.accept()));
                clients.add(clientHandler);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server fell down! Bad socket!");
            e.printStackTrace();
        } finally {
            System.out.println("Closing all active connections...");
            for (ClientHandler client : clients) {
                client.connClient.tearConnection();;
            }
        }
    }

    static class ClientHandler implements Runnable {
        public connection connClient;

        public ClientHandler(connection conn) {
            connClient = conn;
        }

        @Override
        public void run() {
            try {
                String clientMessage = connClient.recieveString();
                while (clientMessage != null) {
                    String toClient = "Welcome on board!";

                    // add logic to handle incomming messages
                    System.out.println("Recieved from client: [" + clientMessage + "]");
                    connClient.sendString(toClient + " You sent: " + clientMessage);

                    clientMessage = connClient.recieveString();
                }
            } catch (IOException e) {
                System.out.println("Can not send reply back to client!");
                e.printStackTrace();
            }
        }
    }
}
