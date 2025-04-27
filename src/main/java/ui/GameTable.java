package ui;

import javafx.scene.Group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import connection.Connection;
import model.*;

public class GameTable extends Group {
    private HandDeck playerDeck;
    private HandDeck opponentDeck;
    private BazarTiles tableTiles;

    private List<SpriteTile> placedTiles = new ArrayList<>();

    private double width;
    private double hight;

    private serverHandler handlerResponse;
    private updateHandler handlerUpdate;

    public GameTable(Connection server, double width, double hight) {
        this.width = width;
        this.hight = hight;

        initPlayerDeck();
        initTableTiles();

        getChildren().addAll(playerDeck, opponentDeck, tableTiles);

        // to recieve server updates
        handlerResponse = new serverHandler(server);
        new Thread(handlerResponse).start();

        // to handle resize or chain translate
        handlerUpdate = new updateHandler();
        new Thread(handlerUpdate).start();
    }

    public void addTileInDeck(Tile newTile) {
        playerDeck.addTile(new SpriteTile(newTile));
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
            Thread.currentThread().interrupt();
            return new GameResponse(ResponseType.UNKNOWN);
        }
    }

    private void initTableTiles() {
        double tilesHight = hight / 4;
        double tilesWidth = hight / 4;
        double tilesPosX  = 10;
        double tilesPosY  = tilesHight + hight / 8;

        tableTiles = new BazarTiles(tilesWidth, tilesHight, tilesPosX, tilesPosY);
    }

    private void initPlayerDeck () {
        double deckHight  = hight / 4;
        double deckWidth  = width * 0.9;
        double playerPosX = 0.05 * width;
        double playerPosY = 3 * hight / 4 - 10; // 10 is a small offset from screen buttom

        playerDeck = new HandDeck(deckWidth, deckHight, playerPosX, playerPosY);

        playerPosY = 10;
        opponentDeck = new HandDeck(deckWidth, deckHight, playerPosX, playerPosY);
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
                    serverResponses.add(new GameResponse(serverMessage));
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
            GameResponse updateResponse = getResponse(ResponseType.UPDATE);

            System.out.println("---------------Recieved update for table tiles sizes!-------------------");
            System.out.println(updateResponse.toString());

            for (SpriteTile placedTile : placedTiles) {
                placedTile.applyUpdate(updateResponse.getUpdate());
            }
        }
    }

}
