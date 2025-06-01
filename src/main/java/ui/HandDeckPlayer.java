package ui;
import model.Tile;

public class HandDeckPlayer extends HandDeck{
        public HandDeckPlayer(double width, double hight, double posX, double posY)  {
            super(width, hight, posX, posY);
    }

    public void removeTile(SpriteTile tile) {
        getChildren().remove(tile);
        tiles.remove(tile.getTile());
    }


    public void addTile(Tile tile) {
        tiles.add(tile);
        getChildren().add(new SpriteTile(tile, true));
    }

    public void addTile(SpriteTile tile) {
        tile.setTranslateX(0);
        tile.setTranslateY(0);
        tile.setLayoutX(0);
        tile.setLayoutY(0);

        tiles.add(tile.getTile());
        getChildren().add(tile);
    }
}
