package Animation;

import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

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
                anchorMouseX = event.getX();
                anchorMouseY = event.getY();
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
