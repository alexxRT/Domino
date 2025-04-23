package server;

import connection.Connection;
import game.DominoGame;
import model.*;

public class GameSession {
    private Connection playerOne;
    private Connection playerTwo;

    private int numActivePlayers = 0;
    private DominoGame game;

    // first connected player makes move first
    private PlayerID whoseMove = PlayerID.FIRST_PLAYER;

    public GameSession() {};

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

    public boolean addPlayer(Connection newPlayer) {
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

            new Thread(new PlayerHandler(PlayerID.FIRST_PLAYER, playerOne, this));
            new Thread(new PlayerHandler(PlayerID.SECOND_PLAYER, playerTwo, this));
        }
        return true;
    }

    public GameResponse processCommand(String command) {
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
