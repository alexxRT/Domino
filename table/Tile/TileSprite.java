package Tile;

import java.io.*;

import Animation.dragMouse;
import Animation.moveDesire;
import Animation.popUp;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TileSprite extends ImageView {
    private int left;
    private int right;

    private double originX;
    private double originY;

    // animatioins effects
    private popUp slideTile = new popUp();
    private dragMouse dragTile = new dragMouse(this);
    private moveDesire moveTile = new moveDesire();

    public TileSprite(int left_value, int right_value, double posX, double posY) {
        left    = left_value;
        right   = right_value;
        originX = posX;
        originY = posY;

        //String imagePath = "sprite/" + left + "_" + right + ".png";
        String imagePath = "Tile/sprites/dominoTile.png";
        try {
            File tilePNG = new File(imagePath);
            setImage(new Image(tilePNG.toURI().toString()));
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }

        // seting tile position on a scene
        setLayoutX(originX);
        setLayoutY(originY);

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
