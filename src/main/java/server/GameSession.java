package server;

import java.io.IOException;
import connection.Connection;
import game.DominoGame;
import model.*;
import java.util.*;

public class GameSession {
    private PlayerData[] players = new PlayerData[2];
    private int numActivePlayers = 0;
    private DominoGame game;

    public GameSession() {
        game = new DominoGame(1000, 500, new Position(0, 0));
    };

    public boolean checkVacant() {
        if (numActivePlayers < 2)
                return true;
        return false;
    }

    public boolean addPlayer(Connection newPlayer) {
        if (!checkVacant())
            return false;

        insertNewPlayer(new PlayerData(newPlayer));
        numActivePlayers += 1;

        return true;
    }

    public ArrayList<Response> processCommand(Response command) {
        try {
            GameResponse commandBody = command.getBody();
            PlayerData player = getPlayer(command.getConnection());
            PlayerData opponent = getOpponent(command.getConnection());

            ArrayList<Response> handledResp = new ArrayList<>();

            switch (commandBody.getType()) {
                case JOIN_SESSION: // get six tiles on the game start
                    command.setBody(new GameResponse(ResponseType.JOIN_SESSION, Status.OK));
                    handledResp.add(command);
                    handledResp.addAll(handleJoinSession(player));
                    break;

                case PLACE_MOVE:
                    handledResp.addAll(handlePlaceTile(player, opponent, commandBody.getTile()));
                    handledResp.addAll(handleGameOver(player, opponent));
                    break;

                case GET_TILE: // get random tile on request
                    handledResp.addAll(handleGetTile(player, opponent));
                    handledResp.addAll(handleGameOver(player, opponent));
                    break;

                case UNKNOWN:
                default:
                    System.out.println("Command: " + commandBody.toString() + " is not handled explicitely");
                    command.setBody(new GameResponse(ResponseType.UNKNOWN, Status.ERROR));
                    handledResp.add(command);
            }

            return handledResp;

        } catch (Exception e) {
            System.err.println("Error processing command: " + e.getMessage());
            GameResponse errorResponse = new GameResponse(ResponseType.UNKNOWN, Status.ERROR);
            command.setBody(errorResponse);
            return new ArrayList<Response>(Arrays.asList(command));
        }
    }

    private ArrayList<Response> handleJoinSession(PlayerData player) throws IOException {
        ArrayList<Response> onJoin = new ArrayList<>();

        // if all players joined -> randomly decide who starts first
        if (numActivePlayers == 2) {
            GameResponse startTheGame = new GameResponse(ResponseType.MAKE_MOVE, Status.OK);
            int whoStarts = new Random().nextInt(players.length);
            onJoin.add(new Response(players[whoStarts].getConnection(), startTheGame));
        }
        // or simply send 6 tiles to init hand deck on start
        for (int i = 0; i < DominoGame.initialNumTiles; i ++) {
            GameResponse newTile = game.getRandomTile();
            onJoin.add(new Response(player.getConnection(), newTile));
            player.getTiles().add(newTile.getTile());
        }
        return onJoin;
    }

    private ArrayList<Response> handleGetTile(PlayerData player, PlayerData opponent) throws IOException {
        ArrayList<Response> onGetTile = new ArrayList<>();

        GameResponse takeResponse = game.getRandomTile(player.getTiles());
        onGetTile.add(new Response(player.getConnection(), takeResponse));

        if (takeResponse.getStatus() == Status.OK) {
            GameResponse tileTaken = new GameResponse(ResponseType.UPDATE_HAND);
            onGetTile.add(new Response(opponent.getConnection(), tileTaken));
        }

        return onGetTile;
    }

    private ArrayList<Response> handlePlaceTile(PlayerData player, PlayerData opponent, Tile tile) throws IOException {
        ArrayList<Response> onPlaceTile = new ArrayList<>();

        GameResponse placeResponse = game.placeTile(tile);
        onPlaceTile.add(new Response(player.getConnection(), placeResponse));

        if (placeResponse.getStatus() == Status.OK) {
            GameResponse newTilePlaced = new GameResponse(ResponseType.UPDATE_MOVE);
            GameResponse makeMove = new GameResponse(ResponseType.MAKE_MOVE);

            newTilePlaced.setTile(placeResponse.getTile());

            onPlaceTile.add(new Response(opponent.getConnection(), newTilePlaced)); // send opponent user move to update his/her table
            onPlaceTile.add(new Response(opponent.getConnection(), makeMove)); // transfer move right to next player

            player.getTiles().remove(tile); // remove tile from user deck on success placement
        }

        return onPlaceTile;
    }

    private ArrayList<Response> handleGameOver(PlayerData player, PlayerData opponent) {
        return new ArrayList<>();
    }

    private PlayerData getOpponent(Connection player) {
        if (player == players[0].getConnection()) {
            return players[1];
        }
        return players[0];
    }

    private PlayerData getPlayer(Connection player) {
        if (player == players[0].getConnection()) {
            return players[0];
        }
        return players[1];
    }

    private void insertNewPlayer(PlayerData newPlayer) {
        if (players[0] == null || !players[0].isActive()) {
            if (players[0] == null)
                players[0] = newPlayer;
            else {
                newPlayer.setTiles(players[0].getTiles());
                players[0] = newPlayer;
            }
            return;
        }

        if (players[1] == null || !players[1].isActive()) {
            if (players[1] == null)
                players[1] = newPlayer;
            else {
                newPlayer.setTiles(players[1].getTiles());
                players[1] = newPlayer;
            }
        }
    }

    public Connection getPlayerOne() {
        return players[0].getConnection();
    }

    public Connection getPlayerTwo() {
        return players[1].getConnection();
    }

    public void close() {
        if (players[0] != null)
            players[0].getConnection().tearConnection();

        if (players[1] != null)
            players[1].getConnection().tearConnection();
    }

    private class PlayerData {

        private Connection playerConn;
        private List<Tile> playerTiles = new ArrayList<>();

        public PlayerData(Connection endpoint, List<Tile> tiles) {
            playerConn = endpoint;
            playerTiles = tiles;
        }

        public PlayerData(Connection endpoint) {
            playerConn = endpoint;
        }

        public void setTiles(List<Tile> tiles) {
            playerTiles = tiles;
        }

        public List<Tile> getTiles() { return playerTiles; }

        public Connection getConnection() { return playerConn; }


        public boolean isActive() {
            return playerConn.isConnected();
        }
    }
}
