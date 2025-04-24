package server;

import java.io.*;
import java.net.*;
import java.util.*;

import connection.Connection;

public class DominoServer {
    private static int port = 12345;
    private static List<GameSession> sessions = new ArrayList<>();

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
                // if last session is not vacant (already two users are playing)
                // create new session
                if (!tryAddSession(newPlayer)) {
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

    private static boolean tryAddSession(Connection newPlayer) {
        // if no sessions are running -> create new first one
        if (sessions.size() == 0) {
            GameSession newSession = new GameSession();
            sessions.add(newSession);
            return newSession.addPlayer(newPlayer);
        }
        return sessions.getLast().addPlayer(newPlayer);
    }

    private static void closeAllSessions() {
        System.out.println("Closing all active game sessions...");
        for (GameSession session : sessions) {
            session.close();
        }
    }
}