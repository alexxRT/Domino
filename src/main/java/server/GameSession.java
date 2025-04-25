package server;

import java.util.List;

import connection.Connection;
import game.DominoGame;
import model.*;



public class GameSession {
    private Connection[] players = new Connection[2];

    private int numActivePlayers = 0;
    private DominoGame game;

    public GameSession() {};

    private boolean checkVacant() {
        if (players[0] == null ||
            players[1] == null)
                return true;
        return false;
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
                    return handlePlaceTile(command.getTile(0));
                case BAD_MOVE:
                default:
                    return new GameResponse(ResponseType.BAD_MOVE);
            }
            // check if resize and translate nedded and do it independently
            // return game.translateTileChain();
            // return handleResize();
        } catch (Exception e) {
            System.err.println("Error processing command: " + e.getMessage());
            return new GameResponse(ResponseType.BAD_MOVE);
        }
    }

    private GameResponse handlePlaceTile(Tile tile) {
        return game.placeTile(tile.getLeftVal(), tile.getRightVal());
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
        if (players[0] != null)
            players[0].tearConnection();

        if (players[1] != null)
            players[1].tearConnection();
    }
}
