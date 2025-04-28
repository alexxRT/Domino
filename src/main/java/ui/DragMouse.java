package ui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class DragMouse {
    private SpriteTile toMove;

    private double anchorMouseX;
    private double anchorMouseY;

    private handleMouse eventHandler = new handleMouse();

    public DragMouse(SpriteTile toMove) {
        this.toMove = toMove;
    }
    final class handleMouse implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                // remove tile from deck add to GameTable
                HandDeckPlayer parentDeck = (HandDeckPlayer)toMove.getParent();
                GameTable table = (GameTable)parentDeck.getParent();
                parentDeck.removeTile(toMove);
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

    public void apply(SpriteTile toMove) {
        toMove.setOnMousePressed(eventHandler);
        toMove.setOnMouseDragged(eventHandler);
    }
    public void disable(SpriteTile toMove) {
        toMove.setOnMousePressed(null);
        toMove.setOnMouseDragged(null);
    }
}
