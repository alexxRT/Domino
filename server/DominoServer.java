package server;

import java.io.*;
import java.net.*;
import java.util.*;
import game.DominoGame;
import model.*;
import connection.connection;

public class DominoServer {
    private static final int PORT = 12345;
    private static List<GameSession> sessions = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Domino Server started on port " + PORT);

            while (!serverSocket.isClosed()) {
                connection conn = new connection(serverSocket.accept());
                GameSession session = new GameSession(conn);
                sessions.add(session);

                new Thread(session).start();
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

    static class GameSession implements Runnable {
        private final connection conn;
        private final DominoGame game;

        public GameSession(connection conn) {
            this.conn = conn;
            // Initialize game with default board size
            this.game = new DominoGame(800, 600, new Position(0, 0));
        }

        @Override
        public void run() {
            try {
                handleGameSession();
            } catch (IOException e) {
                System.err.println("Game session error: " + e.getMessage());
            } finally {
                close();
            }
        }

        private void handleGameSession() throws IOException {
            String clientMessage;
            while ((clientMessage = conn.recieveString()) != null) {
                GameResponse response = processCommand(clientMessage);
                conn.sendString(response.toString());
            }
        }

        private GameResponse processCommand(String command) {
            String[] parts = command.split(" ");
            try {
                switch (parts[0].toUpperCase()) {
                    case "PLACE":
                        return handlePlaceTile(parts);
                    case "TRANSLATE":
                        return game.translateTileChain();
                    case "RESIZE":
                        return handleResize();
                    default:
                        return new GameResponse(ResponseType.BAD_MOVE);
                }
            } catch (Exception e) {
                System.err.println("Error processing command: " + e.getMessage());
                return new GameResponse(ResponseType.BAD_MOVE);
            }
        }

        private GameResponse handlePlaceTile(String[] parts) {
            if (parts.length != 3) {
                return new GameResponse(ResponseType.BAD_MOVE);
            }
            try {
                int left = Integer.parseInt(parts[1]);
                int right = Integer.parseInt(parts[2]);
                return game.placeTile(left, right);
            } catch (NumberFormatException e) {
                return new GameResponse(ResponseType.BAD_MOVE);
            }
        }

        private GameResponse handleResize() {
            boolean resized = game.resizeTileChain();
            if (!resized) {
                return new GameResponse(ResponseType.BAD_MOVE);
            }
            // Create response with all tiles after resize
            GameResponse response = new GameResponse(ResponseType.RESIZE);
            for (Tile tile : game.getPlacedTiles()) {
                response.addUpdateTile(tile);
            }
            return response;
        }

        public void close() {
            if (conn != null) {
                conn.tearConnection();
            }
        }
    }
}