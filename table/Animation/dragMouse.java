package Animation;

import Table.handDeck;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
import javafx.scene.*;

public class dragMouse {
    private ImageView toMove;

    private double anchorMouseX;
    private double anchorMouseY;

    private handleMouse eventHandler = new handleMouse();

    public dragMouse(ImageView toMove) {
        this.toMove = toMove;
    }

    final class handleMouse implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {

                // remove tile from deck
                handDeck parentDeck = (handDeck)toMove.getParent();
                Parent root = parentDeck.getScene().getRoot();
                parentDeck.getChildren().remove(toMove);
                ((Group) root).getChildren().add(toMove);

                anchorMouseX = event.getX();
                anchorMouseY = event.getY();

                toMove.setLayoutX(event.getSceneX() - anchorMouseX);
                toMove.setLayoutY(event.getSceneY() - anchorMouseY);

            }
            else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                toMove.setLayoutX(event.getSceneX() - anchorMouseX);
                toMove.setLayoutY(event.getSceneY() - anchorMouseY);
            }
        }
    }

    public void apply(ImageView toMove) {
        toMove.setOnMousePressed(eventHandler);
        toMove.setOnMouseDragged(eventHandler);
    }
    public void disable(ImageView toMove) {
        toMove.setOnMousePressed(null);
        toMove.setOnMouseDragged(null);
    }
}
