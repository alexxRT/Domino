package ui;

import model.Tile;

import javafx.geometry.Pos;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class HandDeck extends HBox {
    private List<Tile> tiles = new ArrayList<>();

    public HandDeck(double width, double higth, double posX, double posY) {
        // setHeight(higth);
        // setWidth(width);
        setPrefSize(width, higth);

        setLayoutX(posX);
        setLayoutY(posY);

        setAlignment(Pos.CENTER);
        setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    public void addTile(SpriteTile tile) {
        if (!tiles.contains(tile.getTile())) {
            tiles.add(tile.getTile());
            getChildren().add(tile);
        }
    }
}
