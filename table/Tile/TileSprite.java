package Tile;

import java.io.*;

import Animation.dragMouse;
import Animation.moveDesire;
import Animation.popUp;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TileSprite extends ImageView {
    private Tile tile;

    // animatioins effects
    private popUp slideTile = new popUp();
    private dragMouse dragTile = new dragMouse(this);
    private moveDesire moveTile = new moveDesire();

    public TileSprite(Tile tile) {
        this.tile = tile;

        //String imagePath = "sprite/" + left + "_" + right + ".png";
        String imagePath;
        if (this.tile.getLeft() < 0 || this.tile.getRight() < 0)
            imagePath = "Tile/sprites/dominoDown.png";
        else
            imagePath = "Tile/sprites/dominoTile.png";

        try {
            File tilePNG = new File(imagePath);
            setImage(new Image(tilePNG.toURI().toString()));
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }

        // temperary DURTY hack to destinguish player and enemies tiles
        if (this.tile.getLeft() + this.tile.getRight() >= 0) {
            slideTile.apply(this);
            dragTile.apply(this);

            // make tile inactive when move placed
            setOnMouseReleased(e -> {
                dragTile.disable(this);
                slideTile.disable(this);

                moveTile.setDesire(e.getSceneX(), e.getSceneY(), 500, 250, 90);
                moveTile.apply(this);

                setOnMouseReleased(null);
            });
        }
    }

    public Tile getTile() {
        return this.tile;
    }

}
