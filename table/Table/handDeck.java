package Table;

import javafx.geometry.Pos;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import Tile.TileSprite;
import Tile.Tile;
import java.util.ArrayList;
import java.util.List;

public class handDeck extends HBox {
    private List<Tile> tiles = new ArrayList<>();

    public handDeck(double width, double higth, double posX, double posY) {
        // setHeight(higth);
        // setWidth(width);
        setPrefSize(width, higth);

        setLayoutX(posX);
        setLayoutY(posY);

        setAlignment(Pos.CENTER);
        setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    public void addTile(TileSprite tile) {
        if (!tiles.contains(tile.getTile())) {
            tiles.add(tile.getTile());
            getChildren().add(tile);
        }
    }
}
