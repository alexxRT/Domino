package server;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import connection.Connection;
import game.DominoGame;
import model.*;



public class GameSession {
    private Connection[] players = new Connection[2];

    private int numActivePlayers = 0;
    private DominoGame game;

    private static final int initialNumTiles = 6;

    public GameSession() {};

    private boolean checkVacant() {
        if (players[0] == null ||
            players[1] == null)
                return true;
        return false;
    }

    public boolean addPlayer(Connection newPlayer) {
        if (!checkVacant())
            return false;

        insertNewPlayer(newPlayer);
        numActivePlayers += 1;

        if (numActivePlayers == 2)
            game = new DominoGame(800, 600, new Position(0, 0));

        return true;
    }

    public GameResponse processCommand(Connection user, GameResponse command, List<Tile> userTiles) {
        try {
            switch (command.getType()) {
                case PLACE_MOVE:
                    GameResponse placeResponse = handlePlaceTile(user, command.getTile(0));
                    if (game.needResize())
                        user.sendString(game.resizeTileChain().toString());
                    if (game.needTranslate())
                        user.sendString(game.translateTileChain().toString());
                    return placeResponse;
                case JOIN_SESSION: // get six tiles on the game start
                    return handleJoinSession(userTiles);
                case GET_TILE: // get random tile on request
                    return game.getRandomTiles(userTiles, 1);
                case BAD_MOVE:
                default:
                    System.out.println("Command: " + command + " is not handled explicitely");
                    return new GameResponse(ResponseType.BAD_MOVE);
            }
        } catch (Exception e) {
            System.err.println("Error processing command: " + e.getMessage());
            return new GameResponse(ResponseType.BAD_MOVE);
        }
    }

    private GameResponse handleJoinSession(List<Tile> userTiles) throws IOException {
        // if all players joined -> randomly decide who starts first
        GameResponse startTheGame = new GameResponse(ResponseType.MAKE_MOVE);
        if (numActivePlayers == 2) {
            int whoStarts = new Random().nextInt(players.length);
            players[whoStarts].sendString(startTheGame.toString());
        }

        return game.getRandomTiles(userTiles, initialNumTiles);
    }

    private GameResponse handlePlaceTile(Connection placingUser, Tile tile) throws IOException {
        GameResponse placeResponse = game.placeTile(tile.getLeftVal(), tile.getRightVal());

        if (placeResponse.getType() != ResponseType.BAD_MOVE) {
            Connection opponent = getOpponent(placingUser);
            opponent.sendString(new GameResponse(ResponseType.MAKE_MOVE).toString());
        }

        return placeResponse;
    }

    private Connection getOpponent(Connection user) {
        if (user == players[0]) {
            return players[1];
        }
        return players[1];
    }

    private void insertNewPlayer(Connection newPlayer) {
        if (players[0] == null) {
            players[0] = newPlayer;
            return;
        }

        if (players[1] == null) {
            players[1] = newPlayer;
            return;
        }
    }

    public void close() {
        if (players[0] != null)
            players[0].tearConnection();

        if (players[1] != null)
            players[1].tearConnection();
    }
}
