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
                connection newPlayer = new connection(serverSocket.accept());

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

    static class GameSession {
        static private enum PlayerID {
            FIRST_PLAYER,
            SECOND_PLAYER,
            OUT_OF_PLAYERS
        }
        private connection playerOne;
        private connection playerTwo;

        private int numActivePlayers = 0;
        private DominoGame game;

        // first connected player makes move first
        private PlayerID whoseMove = PlayerID.FIRST_PLAYER;

        public GameSession() {};

        private class PlayerHandler implements Runnable {
            public PlayerID playerId;
            public PlayerHandler(PlayerID id) {
                playerId = id;
            }
            @Override
            public void run() {
                String clientMessage;
                try {
                    while ((clientMessage = recieveString()) != null) {
                        // first message byte - from which client message recieved
                        GameResponse response = processCommand(playerId + clientMessage);
                        sendString(response.toString());
                    }
                }
                catch (IOException e) {
                    System.out.println("Player " + playerId + " has network issues! Terminating handling...");
                    e.printStackTrace();
                }
            }

            private String recieveString() {
                if (playerId == PlayerID.FIRST_PLAYER)
                    return playerOne.recieveString();
                if (playerId == PlayerID.SECOND_PLAYER)
                    return playerTwo.recieveString();
                String errMsg = new String("Bad player ID on playerHandler instance");
                throw new RuntimeException(errMsg);
            }

            private void sendString(String mString) throws IOException {
                if (playerId == PlayerID.FIRST_PLAYER)
                    playerOne.sendString(mString);
                if (playerId == PlayerID.SECOND_PLAYER)
                    playerTwo.sendString(mString);
                String errMsg = new String("Bad player ID on playerHandler instance");
                throw new RuntimeException(errMsg);
            }
        }

        private PlayerID checkVacant() {
            if (numActivePlayers == 2)
                return PlayerID.OUT_OF_PLAYERS;

            if (playerOne == null)
                return PlayerID.FIRST_PLAYER;

            if (playerTwo == null)
                return PlayerID.SECOND_PLAYER;

            String errMsg = new String("Can not resolve vacant!");
            throw new RuntimeException(errMsg);
        }

        public boolean addPlayer(connection newPlayer) {
            PlayerID playerStat = checkVacant();

            switch (playerStat) {
                case PlayerID.FIRST_PLAYER:
                    playerOne = newPlayer;
                    numActivePlayers += 1;
                    break;
                case PlayerID.SECOND_PLAYER:
                    playerTwo = newPlayer;
                    numActivePlayers += 1;
                    break;
                case PlayerID.OUT_OF_PLAYERS:
                    return false;
                default:
                    String errMsg = new String("Unknow PlayerID on addPlayer");
                    throw new RuntimeException(errMsg);
            }

            // new player successfully added,
            // now check if there are two active players -> we start the game
            // before that non of the user request was handled
            if (checkVacant() == PlayerID.OUT_OF_PLAYERS) {
                game = new DominoGame(800, 600, new Position(0, 0));

                new Thread(new PlayerHandler(PlayerID.FIRST_PLAYER));
                new Thread(new PlayerHandler(PlayerID.SECOND_PLAYER));
            }
            return true;
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
            if (playerOne != null)
                playerOne.tearConnection();

            if (playerTwo != null)
                playerTwo.tearConnection();
        }
    }
}