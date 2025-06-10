package ui;

import model.Position;
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
    protected List<Tile> tiles = new ArrayList<>();

    public HandDeck(double width, double higth, double posX, double posY) {
        setPrefSize(width, higth);

        setLayoutX(posX);
        setLayoutY(posY);

        setAlignment(Pos.CENTER);
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public Position getPosition() {
        return new Position(getLayoutX(), getLayoutY());
    }
}
