package server;

import java.io.*;
import java.net.*;
import java.util.*;

import connection.Connection;


public class DominoServer {
    private static final int PORT = 12345;
    private static List<GameSession> sessions = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Domino Server started on port " + PORT);

            while (!serverSocket.isClosed()) {
                Connection newPlayer = new Connection(serverSocket.accept());

                // trying to add player in last available session currently
                boolean isAdded = sessions.getLast().addPlayer(newPlayer);

                // if last session is not vacant (already two users are playing)
                // create new session
                if (!isAdded) {
                    GameSession newSession = new GameSession();
                    newSession.addPlayer(newPlayer);
                    sessions.add(newSession);
                }
            }
        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
        } finally {
            closeAllSessions();
        }
    }

    private static void closeAllSessions() {
        System.out.println("Closing all active game sessions...");
        for (GameSession session : sessions) {
            session.close();
        }
    }
}