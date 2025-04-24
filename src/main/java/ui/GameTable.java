package ui;

import ui.SpriteTile;
import javafx.scene.Group;
import model.Tile;

public class GameTable extends Group {
    private HandDeck playerDeck;
    private HandDeck aliasDeck;
    private BazarTiles tableTiles;

    private double width;
    private double hight;

    public GameTable(double width, double hight) {
        this.width = width;
        this.hight = hight;

        initPlayerDeck();
        initTableTiles();

        playerDeck.addTile(new SpriteTile(new Tile(0, 0)));
        playerDeck.addTile(new SpriteTile(new Tile(2, 0)));
        playerDeck.addTile(new SpriteTile(new Tile(3, 4)));

        aliasDeck.addTile(new SpriteTile(new Tile(-1, -2)));
        aliasDeck.addTile(new SpriteTile(new Tile(-2, -3)));
        aliasDeck.addTile(new SpriteTile(new Tile(-5, -6)));

        getChildren().addAll(playerDeck, aliasDeck, tableTiles);
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
        aliasDeck = new HandDeck(deckWidth, deckHight, playerPosX, playerPosY);
    }
}
