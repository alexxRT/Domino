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

    public GameSession() {
        game = new DominoGame(1000, 500, new Position(0, 0));
    };

    private boolean checkVacant() {
        if (numActivePlayers < 2)
                return true;
        return false;
    }

    public boolean addPlayer(Connection newPlayer) {
        if (!checkVacant())
            return false;

        insertNewPlayer(newPlayer);
        numActivePlayers += 1;

        return true;
    }

    public GameResponse[] processCommand(Connection user, GameResponse command, List<Tile> userTiles) {
        try {
            switch (command.getType()) {
                case PLACE_MOVE:
                    GameResponse placeResponse = handlePlaceTile(user, command.getTile(), userTiles);
                    System.out.println("[AMADEY] CHAIN leftTile = " + game.leftTile + " rightTile = " + game.rightTile);
                    //if (game.needResize())
                    //    user.sendString(game.resizeTileChain().toString());
                    //if (game.needTranslate())
                    //    user.sendString(game.translateTileChain().toString());
                    return new GameResponse[]{placeResponse};
                case JOIN_SESSION: // get six tiles on the game start
                    System.out.println("[AMADEY] NEW JOIN SESSION");
                    user.sendString(new GameResponse(ResponseType.JOIN_SESSION).toString());
                    return handleJoinSession(userTiles);
                case GET_TILE: // get random tile on request
                    return handleGetTile(user, userTiles);
                case UNKNOWN:
                default:
                    System.out.println("Command: " + command + " is not handled explicitely");
                    GameResponse errorResponse = new GameResponse(ResponseType.UNKNOWN, Status.ERROR);
                    return new GameResponse[]{errorResponse};
            }
        } catch (Exception e) {
            System.err.println("Error processing command: " + e.getMessage());
            GameResponse errorResponse = new GameResponse(ResponseType.UNKNOWN, Status.ERROR);
            return new GameResponse[]{errorResponse};
        }
    }

    private GameResponse[] handleJoinSession(List<Tile> userTiles) throws IOException {
        // if all players joined -> randomly decide who starts first
        if (numActivePlayers == 2) {
            GameResponse startTheGame = new GameResponse(ResponseType.MAKE_MOVE);
            int whoStarts = new Random().nextInt(players.length);
            players[whoStarts].sendString(startTheGame.toString());
        }
        // or simply send 6 tiles to init hand deck on start
        GameResponse[] newTiles = new GameResponse[DominoGame.initialNumTiles];
        for (int i = 0; i < DominoGame.initialNumTiles; i ++) {
            newTiles[i] = game.getRandomTile();
            userTiles.add(newTiles[i].getTile());
            System.out.println("[AMADEY] NEW DOMINO TILE: " + newTiles[i].toString());
        }
        return newTiles;
    }

    private GameResponse[] handleGetTile(Connection user, List<Tile> userTiles) throws IOException {
        Connection opponent = getOpponent(user);
        GameResponse takeResponse = game.getRandomTile(userTiles);

        if (takeResponse.getStatus() == Status.OK) {
            GameResponse tileTaken = new GameResponse(ResponseType.UPDATE);
            opponent.sendString(tileTaken.toString());
        }

        return new GameResponse[]{takeResponse};
    }

    private GameResponse handlePlaceTile(Connection placingUser, Tile tile, List<Tile> userTiles) throws IOException {
        GameResponse placeResponse = game.placeTile(tile.getLowVal(), tile.getHighVal());

        if (placeResponse.getStatus() == Status.OK) {
            Connection opponent = getOpponent(placingUser);
            GameResponse newTilePlaced = new GameResponse(ResponseType.UPDATE);
            newTilePlaced.setTile(placeResponse.getTile());

            opponent.sendString(newTilePlaced.toString()); // send opponent user move to update his/her table
            opponent.sendString(new GameResponse(ResponseType.MAKE_MOVE).toString()); // transfer move right to next player

            userTiles.remove(tile); // remove tile from user deck on success placement
        }

        return placeResponse;
    }

    private Connection getOpponent(Connection user) {
        if (user == players[0]) {
            return players[1];
        }
        return players[0];
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
