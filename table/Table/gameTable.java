package Table;

import Tile.TileSprite;
import javafx.scene.Group;
import Tile.Tile;

public class gameTable extends Group {
    private handDeck playerDeck;
    private handDeck aliasDeck;
    private bazarTiles tableTiles;

    private double width;
    private double hight;

    public gameTable(double width, double hight) {
        this.width = width;
        this.hight = hight;

        initPlayerDeck();
        initTableTiles();

        playerDeck.addTile(new TileSprite(new Tile(0, 0)));
        playerDeck.addTile(new TileSprite(new Tile(2, 0)));
        playerDeck.addTile(new TileSprite(new Tile(3, 4)));

        aliasDeck.addTile(new TileSprite(new Tile(-1, -2)));
        aliasDeck.addTile(new TileSprite(new Tile(-2, -3)));
        aliasDeck.addTile(new TileSprite(new Tile(-5, -6)));

        getChildren().addAll(playerDeck, aliasDeck, tableTiles);
    }

    private void initTableTiles() {
        double tilesHight = hight / 4;
        double tilesWidth = hight / 4;
        double tilesPosX  = 10;
        double tilesPosY  = tilesHight + hight / 8;

        tableTiles = new bazarTiles(tilesWidth, tilesHight, tilesPosX, tilesPosY);
    }

    private void initPlayerDeck () {
        double deckHight  = hight / 4;
        double deckWidth  = width * 0.9;
        double playerPosX = 0.05 * width;
        double playerPosY = 3 * hight / 4 - 10; // 10 is a small offset from screen buttom

        playerDeck = new handDeck(deckWidth, deckHight, playerPosX, playerPosY);

        playerPosY = 10;
        aliasDeck = new handDeck(deckWidth, deckHight, playerPosX, playerPosY);
    }
}
