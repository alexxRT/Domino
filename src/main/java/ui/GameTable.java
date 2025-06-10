package ui;

import javafx.application.Platform;
import javafx.scene.Group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import javafx.scene.control.Label;

import connection.Connection;
import client.DominoClient;
import game.DominoGame;
import model.*;

public class GameTable extends Group {
    private HandDeckPlayer playerDeck;
    private HandDeckOpponent opponentDeck;

    private BazarTiles bazarTiles;

    private List<SpriteTile> placedTiles = new ArrayList<>();

    private double width;
    private double hight;
    private int sessionID;

    private serverHandler handlerResponse;

    private Label statusText;

    @FunctionalInterface
    public interface GameEndCallback {
        void onGameEnd(Status status);
    }
    private GameEndCallback gameEndCallback;

    @FunctionalInterface
    public interface SessionIdCallback {
        void setSessionID(int sessionID);
    }
    private SessionIdCallback setSessionIdCallback;

    public void setGameSessionCallback(SessionIdCallback callback) {
        this.setSessionIdCallback = callback;
    }

    public void setGameEndCallback(GameEndCallback callback) {
        this.gameEndCallback = callback;
    }

    public GameTable(Connection server, double width, double hight) {
        this.width = width;
        this.hight = hight;

        initDecks();
        initBazar();

        statusText = new Label();
        updateStatusText();
        statusText.setLayoutX(820);
        statusText.setLayoutY(320);

        getChildren().addAll(playerDeck, opponentDeck, bazarTiles, statusText);

        // to recieve server updates
        handlerResponse = new serverHandler(server);
        new Thread(handlerResponse).start();
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

    public int getSessionID() {
        return sessionID;
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

    public void updateStatusText() {
        if (DominoClient.dominoOn) {
            statusText.setText("Your move");
            statusText.setStyle(
                "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #FFA500, #FFFF00);" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 12 20 12 20;" +
                "-fx-text-fill: #2F4F4F;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 18px;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.6), 8, 0.5, 0, 2);");
        } else {
            statusText.setText("Opponent move");
            statusText.setStyle(
                "-fx-background-color: rgba(70, 70, 70, 0.8);" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 12 20 12 20;" +
                "-fx-text-fill: #CCCCCC;" +
                "-fx-font-weight: normal;" +
                "-fx-font-size: 16px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 3, 0.3, 0, 1);");
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
                    ResponseType respType = response.getType();

                    if (respType == ResponseType.MAKE_MOVE) {
                        DominoClient.dominoOn = true;
                        Platform.runLater(() -> { updateStatusText(); });
                    } else if (respType == ResponseType.GAME_OVER) {
                        endGame(response.getStatus());
                    } else {
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
    public void startNewGame(int sessionID) {
        try {
            GameResponse askJoin = new GameResponse(ResponseType.JOIN_SESSION);
            askJoin.setSessionID(sessionID);

            getConnection().sendString(askJoin.toString());
            GameResponse response = getResponse(ResponseType.JOIN_SESSION);

            // joined session!
            if (response.getStatus() == Status.OK) {
                for (int i = 0; i < DominoGame.initialNumTiles; i++) {
                    GameResponse newTile = getResponse(ResponseType.GET_TILE);
                    addPlayerDeck(newTile.getTile());
                    addOpponentDeck();
                }
                this.sessionID = response.getSessionID();
                setSessionIdCallback.setSessionID(this.sessionID);
            }
        }
        catch (IOException e) {
            System.out.println("Unable start new game, due to network issues!");
            e.printStackTrace();
        }
    }

    private void endGame(Status status) {
        DominoClient.dominoOn = false;
        if (gameEndCallback != null) {
            gameEndCallback.onGameEnd(status);
        }
    }

}
