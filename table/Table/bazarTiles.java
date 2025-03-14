package Table;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
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

public class bazarTiles extends Region {
    private int numLeft = 28;
    private List<BackgroundImage> statusImages = new ArrayList<>();
    private BackgroundImage currentImage;

    public bazarTiles(double width, double hight, double posX, double posY) {
        setPrefSize(width, hight);

        setLayoutX(posX);
        setLayoutY(posY);

        setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        // load status images
        String spritePath = "Table/sprites/dominos.jpeg";
        try {
            Image spriteImage = new Image(new File(spritePath).toURI().toString());
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
