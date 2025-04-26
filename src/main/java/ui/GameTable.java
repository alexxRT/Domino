package ui;

import javafx.scene.Group;

import java.io.IOException;

import java.util.concurrent.LinkedBlockingDeque;

import connection.Connection;
import model.*;

public class GameTable extends Group {
    private HandDeck playerDeck;
    private HandDeck opponentDeck;
    private BazarTiles tableTiles;

    private double width;
    private double hight;

    private serverHandler handler;

    // TODO: add resize and tiles move handler //

    public GameTable(Connection server, double width, double hight) {
        this.width = width;
        this.hight = hight;

        initPlayerDeck();
        initTableTiles();

        getChildren().addAll(playerDeck, opponentDeck, tableTiles);

        // to recieve server updates
        handler = new serverHandler(server);
        new Thread(handler).start();

    }

    public void addTileInDeck(Tile newTile) {
        playerDeck.addTile(new SpriteTile(newTile));
    }

    public Connection getConnection() {
        return handler.server;
    }

    public GameResponse getResponse(ResponseType type) {
        try {
            GameResponse serverResponse = handler.serverResponses.take();
            while (serverResponse.getType() != type) {
                handler.serverResponses.add(serverResponse);
                serverResponse = handler.serverResponses.take();
            }
            return serverResponse;
        }
        catch (InterruptedException e) {
            System.out.println("Failed to retrieve response! Interrupted!");
            Thread.currentThread().interrupt();
            return new GameResponse(ResponseType.BAD_MOVE);
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
}
