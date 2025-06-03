package client;

import javafx.application.Application;


import ui.GameTable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import connection.Connection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;



public class DominoClient extends Application{

    private static final int CELL_SIZE = 56; // size of each square
    private static final int ROWS = 9;       // number of rows
    private static final int COLS = 27;


    private static Connection dominoServer;
    public static void main(String[] args) {
        String ipAddr = args[1];
        int port = Integer.parseInt(args[2]);

        dominoServer = new Connection(ipAddr, port);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("testBoard");
        Group rootGroup = new Group();

        // ImageView background = new ImageView();
        // try {
        //     String tablePath = getClass().getResource("/dominoTable.jpeg").toExternalForm();
        //     background.setImage(new Image(tablePath));
        // } catch (Exception e) {
        //     System.out.println("Bad image init for tile!");
        //     e.printStackTrace();
        // }
        // background.setFitHeight(500);
        // background.setFitWidth(1000);
        // rootGroup.getChildren().add(background);

        Canvas canvas = new Canvas(COLS * CELL_SIZE, ROWS * CELL_SIZE);
        drawGrid(canvas);

        GameTable table = new GameTable(dominoServer, 1000, 500);
        rootGroup.getChildren().add(canvas);
        rootGroup.getChildren().add(table);

        Scene board = new Scene(rootGroup, 1000, 500);

        primaryStage.setScene(board);
        primaryStage.show();
    }


    private void drawGrid(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double x = col * CELL_SIZE;
                double y = row * CELL_SIZE;

                // Draw filled square (optional)
                gc.setFill(Color.WHITE);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Draw square border
                gc.setStroke(Color.RED);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }
}
