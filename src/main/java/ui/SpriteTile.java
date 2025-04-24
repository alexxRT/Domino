package ui;

import java.io.*;
import model.*;

import ui.DragMouse;
import ui.MoveDesire;
import ui.PopUp;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SpriteTile extends ImageView {
    private Tile tile;

    // animatioins effects
    private PopUp slideTile = new PopUp();
    private DragMouse dragTile = new DragMouse(this);
    private MoveDesire moveTile = new MoveDesire();

    public SpriteTile(Tile tile) {
        this.tile = tile;

        //String imagePath = "sprite/" + left + "_" + right + ".png";
        try {
            String imagePath;
            if (this.tile.getLeftVal() < 0 || this.tile.getRightVal() < 0)
                imagePath = getClass().getResource("/dominoDown.png").toExternalForm();
            else
                imagePath = getClass().getResource("/dominoTile.png").toExternalForm();
            setImage(new Image(imagePath));
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }

        // temperary DURTY hack to destinguish player and enemies tiles
        if (this.tile.getLeftVal() + this.tile.getRightVal() >= 0) {
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
