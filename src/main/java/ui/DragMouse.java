package ui;

import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Group;
import javafx.scene.*;

public class DragMouse {
    private ImageView toMove;

    private double anchorMouseX;
    private double anchorMouseY;

    private handleMouse eventHandler = new handleMouse();

    public DragMouse(ImageView toMove) {
        this.toMove = toMove;
    }
    final class handleMouse implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                // remove tile from deck add to GameTable
                HandDeck parentDeck = (HandDeck)toMove.getParent();
                GameTable table = (GameTable)parentDeck.getParent();
                parentDeck.getChildren().remove(toMove);
                table.getChildren().add(toMove);

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
