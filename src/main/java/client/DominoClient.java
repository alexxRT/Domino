package client;
import javafx.application.Application;
import ui.GameTable;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.stage.Stage;
import connection.Connection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import java.io.*;
import javafx.geometry.*;
import javafx.scene.layout.VBox;
import javafx.scene.effect.GaussianBlur;
import model.*;
import javafx.animation.*;
import javafx.util.*;

public class DominoClient extends Application{
    private static final int CELL_SIZE = 56;
    private static final int ROWS = 9;
    private static final int COLS = 27;
    public static boolean dominoOn = false;

    private static String serverIP;
    private static int serverPort;
    private Connection dominoServer;

    public static void main(String[] args) {
        serverIP = args[1];
        serverPort = Integer.parseInt(args[2]);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DOMINO");
        Group welcome = welcomeWindow(primaryStage);

        Scene welcomeScene = new Scene(welcome, 1000, 500);
        primaryStage.setScene(welcomeScene);
        primaryStage.show();
    }

    private Group welcomeWindow(Stage primaryStage) {
        ImageView welcomeBackground = setWelcomeBackground();
        welcomeBackground.setFitHeight(500);
        welcomeBackground.setFitWidth(1000);

        ImageView buttonImage = setButtonImage("/startButton.png");
        buttonImage.setFitHeight(70);
        buttonImage.setFitWidth(270);

        Button startButton = new Button();
        startButton.setGraphic(buttonImage);
        startButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        startButton.setLayoutX(360);
        startButton.setLayoutY(330);

        addButtonEffects(startButton, buttonImage);
        startButton.setOnAction(e -> startGame(primaryStage));

        Group welcomeGroup = new Group();

        welcomeGroup.getChildren().add(welcomeBackground);
        welcomeGroup.getChildren().add(startButton);

        return welcomeGroup;
    }

    private Group endGameWindow(Stage primaryStage, Status status) {
        ImageView buttonImage = setButtonImage("/newGame.png");
        buttonImage.setFitWidth(300);
        buttonImage.setFitHeight(150);

        Button resetButton = new Button();
        resetButton.setGraphic(buttonImage);
        resetButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        addButtonEffects(resetButton, buttonImage);
        resetButton.setOnAction(e -> resetGame(primaryStage));

        Text endGameText = new Text();
        endGameText.setFont(Font.font("Arial", FontWeight.BOLD, 100));
        endGameText.setStroke(Color.BLACK);
        endGameText.setStrokeWidth(4.0);
        endGameText.setFill(Color.ORANGE);

        switch (status) {
            case VICTORY:
                endGameText.setText("YOU WON!");
                break;
            case DRAW:
                endGameText.setText("DRAW");
                break;
            case DEFEAT:
                endGameText.setText("YOU LOSE!");
                break;
            default:
                endGameText.setText("UNKNOWN");
            endGameText.applyCss();
        }

        double stageWidth = primaryStage.getWidth();
        double stageHeight = primaryStage.getHeight();
        double textWidth = endGameText.getBoundsInLocal().getWidth();

        resetButton.setLayoutX(stageWidth / 2 - 300 / 2);
        resetButton.setLayoutY(stageHeight - 70 * 4);
        endGameText.setLayoutX(stageWidth / 2 - textWidth / 2);
        endGameText.setLayoutY(stageHeight / 2 - 100);

        Group endGameGroup = new Group();
        endGameGroup.getChildren().addAll(resetButton, endGameText);

        animateScaleUp(resetButton, 1);
        animateScaleUp(endGameText, 0.5);

        return endGameGroup;
    }

    private void startGame(Stage primaryStage) {
        try {
            Group rootGroup = new Group();
            dominoServer = new Connection(serverIP, serverPort);
            primaryStage.setTitle("testBoard");
            ImageView background = setGameBackground();
            background.setFitHeight(500);
            background.setFitWidth(1000);
            rootGroup.getChildren().add(background);

            Canvas canvas = new Canvas(COLS * CELL_SIZE, ROWS * CELL_SIZE);
            drawGrid(canvas);

            GameTable table = new GameTable(dominoServer, 1000, 500);
            table.setGameEndCallback(status -> gameOver(primaryStage, table, status));

            rootGroup.getChildren().add(canvas);
            rootGroup.getChildren().add(table);

            Scene startGame = new Scene(rootGroup, 1000, 500);
            primaryStage.setScene(startGame);
            primaryStage.show();
        } catch (IOException badConnection) {
            Group rootGroup = (Group)primaryStage.getScene().getRoot();

            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setStyle("-fx-progress-color: orange; -fx-accent: #FFD700;");
            spinner.setPrefSize(80, 80);

            spinner.setLayoutX(480);
            spinner.setLayoutY(420);

            if (primaryStage.getTitle() == "DOMINO")
                rootGroup.getChildren().add(spinner);

            primaryStage.setTitle("Connecting...");
            System.out.println("Bad connection attempt!");
            badConnection.printStackTrace();
        }
    }

    private void gameOver(Stage primaryStage, GameTable table, Status status) {
        Group backgroundGroup = new Group();

        backgroundGroup.getChildren().add(table);
        backgroundGroup.setEffect(new GaussianBlur(10));

        table.getChildren().clear();
        table.getChildren().add(backgroundGroup);

        table.getChildren().add(endGameWindow(primaryStage, status));
    }

    private void resetGame(Stage primaryStage) {
        primaryStage.setTitle("DOMINO");
        Group welcome = welcomeWindow(primaryStage);

        Scene welcomeScene = new Scene(welcome, 1000, 500);
        primaryStage.setScene(welcomeScene);
        primaryStage.show();
    }

    private ImageView setWelcomeBackground() {
        ImageView background = new ImageView();
        try {
            String backgroundPath = getClass().getResource("/welcomeWindow.jpg").toExternalForm();
            background.setImage(new Image(backgroundPath));
        } catch (Exception e) {
            System.out.println("Bad image init for welcome background!");
            e.printStackTrace();
        }
        return background;
    }

    private ImageView setButtonImage(String imagePath) {
        ImageView buttonImg = new ImageView();
        try {
            String buttonPath = getClass().getResource(imagePath).toExternalForm();
            buttonImg.setImage(new Image(buttonPath));
        } catch (Exception e) {
            System.out.println("Bad image init for start button!");
            e.printStackTrace();
        }
        return buttonImg;
    }


    private ImageView setGameBackground() {
        ImageView background = new ImageView();
        try {
            String tablePath = getClass().getResource("/dominoTable.jpeg").toExternalForm();
            background.setImage(new Image(tablePath));
        } catch (Exception e) {
            System.out.println("Bad image init for tile!");
            e.printStackTrace();
        }
        return background;
    }

    private void addButtonEffects(Button button, ImageView buttonImage) {
        button.setOnMouseEntered(e -> {
            buttonImage.setStyle("-fx-effect: innershadow(gaussian, rgba(0,0,0,0.5), 15, 0.8, 0, 0);");
        });

        button.setOnMouseExited(e -> {
            buttonImage.setScaleX(1.0);
            buttonImage.setScaleY(1.0);
            buttonImage.setStyle("-fx-effect: null;");
        });

        button.setOnMousePressed(e -> {
            buttonImage.setScaleX(0.95);
            buttonImage.setScaleY(0.95);
            buttonImage.setStyle("-fx-effect: innershadow(gaussian, rgba(0,0,0,0.7), 8, 0.5, 0, 2);");
        });

        button.setOnMouseReleased(e -> {
            buttonImage.setScaleX(1.0);
            buttonImage.setScaleY(1.0);

            if (button.isHover())
                buttonImage.setStyle("-fx-effect: innershadow(gaussian, rgba(0,0,0,0.5), 15, 0.8, 0, 0);");
            else
                buttonImage.setStyle("-fx-effect: null;");
        });
    }

    private void drawGrid(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double x = col * CELL_SIZE;
                double y = row * CELL_SIZE;
                gc.setStroke(Color.RED);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void animateScaleUp(Node node, double delaySeconds) {
        node.setScaleX(0);
        node.setScaleY(0);
        node.setOpacity(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.5), node);
        scaleTransition.setFromX(0);
        scaleTransition.setFromY(0);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.seconds(0.5),
                new KeyValue(node.opacityProperty(), 1.0)
            )
        );

        scaleTransition.setDelay(Duration.seconds(delaySeconds));
        fadeIn.setDelay(Duration.seconds(delaySeconds));

        scaleTransition.play();
        fadeIn.play();
    }
}