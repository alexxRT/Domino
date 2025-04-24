package client;

import javafx.application.Application;
import java.io.File;


import ui.GameTable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DominoClient extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("testBoard");
        Group rootGroup = new Group();

        ImageView background = new ImageView();
        try {
            String tablePath = getClass().getResource("/dominoTable.jpeg").toExternalForm();
            background.setImage(new Image(tablePath));
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }
        background.setFitHeight(500);
        background.setFitWidth(1000);
        rootGroup.getChildren().add(background);

        GameTable table = new GameTable(1000, 500);
        rootGroup.getChildren().add(table);

        Scene board = new Scene(rootGroup, 1000, 500);
        primaryStage.setScene(board);
        primaryStage.show();
    }
}