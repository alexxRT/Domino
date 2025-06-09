package ui;

import javafx.application.Platform;
import javafx.scene.Group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import connection.Connection;
import game.DominoGame;
import model.*;

public class GameTable extends Group {
    private HandDeckPlayer playerDeck;
    private HandDeckOpponent opponentDeck;

    private BazarTiles bazarTiles;

    private List<SpriteTile> placedTiles = new ArrayList<>();

    private double width;
    private double hight;

    private serverHandler handlerResponse;

    public GameTable(Connection server, double width, double hight) {
        this.width = width;
        this.hight = hight;

        initDecks();
        initBazar();

        getChildren().addAll(playerDeck, opponentDeck, bazarTiles);

        // to recieve server updates
        handlerResponse = new serverHandler(server);
        new Thread(handlerResponse).start();

        startNewGame();
    }

    public void addPlayerDeck(Tile tile) {
        playerDeck.addTile(tile);
    }

    public void removePlayerDeck(SpriteTile removeTile) {
        playerDeck.removeTile(removeTile);
    }

    public void addOpponentDeck() {
        opponentDeck.addTile();
    }

    public void addTile(SpriteTile addTile) {
        placedTiles.add(addTile);
        getChildren().add(addTile);
    }

    public void removeTile(SpriteTile removeTile) {
        placedTiles.remove(removeTile);
        getChildren().remove(removeTile);
    }

    public Connection getConnection() {
        return handlerResponse.server;
    }


    public GameResponse getResponse(ResponseType type) {
        try {
            GameResponse serverResponse = handlerResponse.serverResponses.take();
            while (serverResponse.getType() != type) {
                handlerResponse.serverResponses.add(serverResponse);
                serverResponse = handlerResponse.serverResponses.take();
            }
            return serverResponse;
        }
        catch (InterruptedException e) {
            System.out.println("Failed to retrieve response! Interrupted!");
            return new GameResponse(ResponseType.UNKNOWN, Status.ERROR);
        }
    }

    private void initBazar() {
        double tilesHight = hight / 4;
        double tilesWidth = hight / 4;
        double tilesPosX  = 10;
        double tilesPosY  = tilesHight + hight / 8;

        bazarTiles = new BazarTiles(tilesWidth, tilesHight, tilesPosX, tilesPosY);
    }

    private void initDecks () {
        double deckHight  = hight / 4;
        double deckWidth  = width * 0.9;
        double playerPosX = 0.05 * width;
        double playerPosY = 3 * hight / 4 - 10; // 10 is a small offset from screen buttom

        playerDeck = new HandDeckPlayer(deckWidth, deckHight, playerPosX, playerPosY);
        playerPosY = 10;
        opponentDeck = new HandDeckOpponent(deckWidth, deckHight, playerPosX, playerPosY);
    }

    private class serverHandler implements Runnable {
        public Connection server;
        public LinkedBlockingDeque<GameResponse> serverResponses = new LinkedBlockingDeque<>();

        public serverHandler(Connection server) {
            this.server = server;
        }

        @Override
        public void run() {
            String serverMessage;
            try {
                while ((serverMessage = server.recieveString()) != null) {
                    System.err.println("Recieved from backend: " + serverMessage);
                    GameResponse response = new GameResponse(serverMessage);

                    if (response.getType() == ResponseType.MAKE_MOVE)
                        DominoGame.dominoOn = true;
                    else {
                        boolean isHandled = handleUpdate(response);
                        if (!isHandled)
                            serverResponses.add(response);
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Can not recieve message from server!");
                serverResponses.notifyAll(); // wake UI threads if any awaited for update
            }
        }
    }

    private boolean handleUpdate(GameResponse update) {
        // place new tile that was done on remote, syncing update localy
        if (update.getType() == ResponseType.UPDATE_MOVE) {
            Tile placeTile = update.getTile();
            SpriteTile placeTileSprite = new SpriteTile(placeTile);

            Platform.runLater(() -> {
                opponentDeck.removeTile();
                addTile(placeTileSprite);
                placeTileSprite.translateDesire(opponentDeck.getLayoutX() + opponentDeck.getWidth() / 2,
                opponentDeck.getLayoutY() + opponentDeck.getHeight() / 2, placeTile);
            });
            return true;
        }

        // update if oppenent took tile from bazar
        if (update.getType() == ResponseType.UPDATE_HAND) {
            Platform.runLater(() -> {
                opponentDeck.addTile();
            });
            return true;
        }

        // update placed tiles position and sizes to fit table
        if (update.getType() == ResponseType.UPDATE_POS) {
            System.out.println("Handling domino tile resize");
            for (SpriteTile tile : placedTiles) {
                tile.applyUpdate(update.getUpdate());
            }
            return true;
        }
        return false;
    }

    // this function requests on server new tiles on game init
    // after that, client can recieve responses from server
    private void startNewGame() {
        try {
            GameResponse askJoin = new GameResponse(ResponseType.JOIN_SESSION);
            getConnection().sendString(new GameResponse(ResponseType.JOIN_SESSION).toString());
            GameResponse response = getResponse(ResponseType.JOIN_SESSION);

            // joined session!
            if (response.getStatus() == Status.OK) {
                for (int i = 0; i < DominoGame.initialNumTiles; i++) {
                    GameResponse newTile = getResponse(ResponseType.GET_TILE);
                    addPlayerDeck(newTile.getTile());
                    addOpponentDeck();
                }
            }

            System.out.println("Recieved all tiles!");
        }
        catch (IOException e) {
            System.out.println("Unable start new game, due to network issues!");
            e.printStackTrace();
        }

    }
}
