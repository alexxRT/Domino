package ui;

public class HandDeckPlayer extends HandDeck{
        public HandDeckPlayer(double width, double hight, double posX, double posY)  {
            super(width, hight, posX, posY);
    }

    public void removeTile(SpriteTile tile) {
        getChildren().remove(tile);
        tiles.remove(tile.getTile());
    }

    public void addTile(SpriteTile tile) {
        tiles.add(tile.getTile());
        getChildren().add(tile);
    }
}
