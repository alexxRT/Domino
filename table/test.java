import javafx.application.Application;

import java.io.File;

import Table.gameTable;
import Tile.TileSprite;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class test extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("testBoard");
        Group rootGroup = new Group();

        ImageView background = new ImageView();
        String tablePath = "Tile/sprites/dominoTable.jpeg";
        try {
            File tablePNG = new File(tablePath);
            background.setImage(new Image(tablePNG.toURI().toString()));
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }
        background.setFitHeight(500);
        background.setFitWidth(1000);
        rootGroup.getChildren().add(background);

        gameTable table = new gameTable(1000, 500);
        rootGroup.getChildren().add(table);

        Scene board = new Scene(rootGroup, 1000, 500);
        primaryStage.setScene(board);
        primaryStage.show();
    }
}
