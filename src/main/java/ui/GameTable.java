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
    private updateHandler handlerUpdate;

    public GameTable(Connection server, double width, double hight) {
        this.width = width;
        this.hight = hight;

        initDecks();
        initBazar();

        getChildren().addAll(playerDeck, opponentDeck, bazarTiles);

        // to recieve server updates
        handlerResponse = new serverHandler(server);
        new Thread(handlerResponse).start();

        // to handle resize or chain translate
        handlerUpdate = new updateHandler();
        new Thread(handlerUpdate).start();

        startNewGame();
    }

    public void addPlayerDeck(Tile newTile) {
        playerDeck.addTile(new SpriteTile(newTile, true));
    }

    public void addOpponentDeck() {
        opponentDeck.addTile();
    }

    public void removeTileFromDeck(SpriteTile removeTile) {
        playerDeck.removeTile(removeTile);
    }

    public void placeTile(SpriteTile placeTile) {
        placedTiles.add(placeTile);
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
                    else
                        serverResponses.add(response);
                }
            }
            catch (IOException e) {
                System.out.println("Can not recieve message from server!");
                serverResponses.notifyAll(); // wake UI threads if any awaited for update
            }
        }
    }

    private class updateHandler implements Runnable {
        @Override
        public void run() {

            while (true) {
                GameResponse updateResponse = getResponse(ResponseType.UPDATE);
                Tile responseTile = updateResponse.getTile();

                if (responseTile.getLeftVal() + responseTile.getRightVal() <= 12) {
                    SpriteTile placeTile = new SpriteTile(responseTile);
                    Platform.runLater(() -> {
                        opponentDeck.removeTile();
                        getChildren().add(placeTile);
                        placeTile.translateDesire(opponentDeck.getLayoutX() + opponentDeck.getWidth() / 2,
                        opponentDeck.getLayoutY() + opponentDeck.getHeight() / 2,
                        responseTile.getX(), responseTile.getY(), responseTile.getRotateDegree());
                    });
                }
                else { // if tile value 7 | 7, opponent took tile -> update on client
                    Platform.runLater(() -> {
                        opponentDeck.addTile();
                    });
                }
                // RESIZE AND TRANSLATE WORKS INCORRECT
                // TODO: DEBUG THIS; STATUS: POSTPONED
                // apply geometrical update on placed tiles if needed
                //if (updateResponse.getUpdate().getResize() != 0
                //|| updateResponse.getUpdate().getDeltaX() != 0) {
                //    for (SpriteTile placedTile : placedTiles)
                //        placedTile.applyUpdate(updateResponse.getUpdate());
                //}
            }
        }
    }

    // this function requests on server new tiles on game init
    // after that, client can recieve responses from server
    private void startNewGame() {
        try {
            GameResponse askJoin = new GameResponse(ResponseType.JOIN_SESSION);
            getConnection().sendString(new GameResponse(ResponseType.JOIN_SESSION).toString());

            System.out.println("Sending on backend: " + askJoin.toString());

            GameResponse response = getResponse(ResponseType.JOIN_SESSION);

            System.out.println("recieved from server: " + response.toString());

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
