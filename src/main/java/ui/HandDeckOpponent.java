package ui;
import model.*;

public class HandDeckOpponent extends HandDeck {

    private static int tileID = 0;

    public HandDeckOpponent(double width, double hight, double posX, double posY) {
        super(width, hight, posX, posY);
    }

    // no matter which to remove
    public void removeTile() {
        var children = getChildren();
        children.remove(children.size() - 1);
        tiles.remove(tiles.size() - 1);
    }


    public void addTile() {
        SpriteTile opponentTile = new SpriteTile(new Tile());
        opponentTile.setID(tileID); // ID needs so FX destinguish nodes
        // otherwise getChildren().add() throws IlligalArgumentException

        tiles.add(opponentTile.getTile());
        getChildren().add(opponentTile);

        tileID += 1;
    }
}
