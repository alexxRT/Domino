package ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.GameResponse;
import model.ResponseType;
import model.Status;
import connection.Connection;
import client.DominoClient;

public class BazarTiles extends Region {
    private int numLeft = 28;
    private List<BackgroundImage> statusImages = new ArrayList<>();
    private BackgroundImage currentImage;

    private TakeHandler takeHandler = new TakeHandler();

    public BazarTiles(double width, double hight, double posX, double posY) {
        setPrefSize(width, hight);

        setLayoutX(posX);
        setLayoutY(posY);

        // load status images
        String spritePath = getClass().getResource("/dominos.jpeg").toExternalForm();
        try {
            Image spriteImage = new Image(spritePath);
            BackgroundImage image = new BackgroundImage(spriteImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(width, hight, false, false, isCache(), false));
            statusImages.add(image);
        }
        catch (Exception e) {
            System.out.println("Unable to load status image for Bazar Tiles!");
            e.printStackTrace();
        }
        setBackground(new Background(getStatusImage()));

        this.setOnMousePressed(takeHandler);
    }

    public GameTable getTable() {
        return (GameTable)this.getParent();
    }

    final class TakeHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            try {
                if (!DominoClient.dominoOn) {
                    System.out.println("Not your move! Can not take new tile!");
                    return;
                }

                GameTable table = getTable();
                Connection dominoServer = table.getConnection();
                int sessionID = table.getSessionID();

                GameResponse tileRequest = new GameResponse(ResponseType.GET_TILE);
                tileRequest.setSessionID(sessionID);

                dominoServer.sendString(tileRequest.toString());
                System.out.println(tileRequest.toString());
                GameResponse takeResponse = table.getResponse(ResponseType.GET_TILE);

                if (takeResponse.getStatus() == Status.OK) {
                    takeTile();
                    System.out.println("Adding tile after taking from bazar!");
                    table.addPlayerDeck(takeResponse.getTile());
                }
            }
            catch (IOException e) {
                System.out.println("Bazar tiles failed to communicate server!");
                e.printStackTrace();
            }
        }
    }

    public void takeTile() {
        if (numLeft == 0) {
            Text textMessage = new Text("No tiles left!");
            textMessage.setFill(Color.RED);
            getChildren().add(textMessage);

            return;
        }
        numLeft --;
        BackgroundImage newImage = getStatusImage();
        // update status image if changed
        if (newImage != currentImage) {
            setBackground(new Background(newImage));
            currentImage = newImage;
        }
    }

    private BackgroundImage getStatusImage() {
        if (statusImages.size() == 0) {
            String message = "Status images does not inited on getStatusIndex!";
            throw new RuntimeException(message);
        }
        int imageIndex = statusImages.size() / numLeft;
        return statusImages.get(imageIndex);
    }
}
