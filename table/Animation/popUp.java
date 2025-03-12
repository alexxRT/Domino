package Animation;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.event.*;

public class popUp {
    private TranslateTransition onMouseEnter;
    private Duration enterStart = Duration.millis(0);

    private TranslateTransition onMouseExit;
    private Duration exitStart = Duration.millis(500);

    private static Duration moveDuration = Duration.millis(500);
    private static double moveOnChoice = 20;

    handleMouse eventHandler = new handleMouse();

    public void apply(ImageView toMove) {
        onMouseEnter = new TranslateTransition(moveDuration, toMove);
        onMouseExit  = new TranslateTransition(moveDuration, toMove);
        onMouseEnter.setInterpolator(Interpolator.LINEAR);
        onMouseExit.setInterpolator(Interpolator.LINEAR);

        toMove.setOnMouseEntered(eventHandler);
        toMove.setOnMouseExited(eventHandler);
    }

    public void disable(ImageView toMove) {
        onMouseEnter.stop();
        onMouseExit.stop();

        toMove.setOnMouseEntered(null);
        toMove.setOnMouseExited(null);
    }

    final class handleMouse implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
                slideFront();
            else if (event.getEventType() == MouseEvent.MOUSE_EXITED)
                slideBack();
        }
    };

    private void slideFront() {
        onMouseExit.pause();
        double moveByY       = -moveOnChoice;
        double totalDuration = moveDuration.toMillis();
        double exitStartMs   = exitStart.toMillis();

        double pauseTime = onMouseExit.getCurrentTime().toMillis();
        if (pauseTime != 0) {
            onMouseExit.stop();
            enterStart = Duration.millis(exitStartMs - pauseTime);
            moveByY    = -moveOnChoice * (totalDuration - enterStart.toMillis()) / totalDuration;
        }

        onMouseEnter.setByY(moveByY);
        onMouseEnter.setDuration(moveDuration.subtract(enterStart));
        onMouseEnter.play();
    }

    private void slideBack() {
        onMouseEnter.pause();
        double moveByY       = 0;
        double totalDuration = moveDuration.toMillis();
        double enterStartMs  = enterStart.toMillis();

        double pauseTime = onMouseEnter.getCurrentTime().toMillis();
        if (pauseTime != 0) {
            onMouseEnter.stop();
            exitStart = Duration.millis(pauseTime + enterStartMs);
            moveByY   = moveOnChoice * exitStart.toMillis() / totalDuration;
        }

        onMouseExit.setByY(moveByY);
        onMouseExit.setDuration(exitStart);
        onMouseExit.play();
    }
}
