package ui;

import javafx.util.Duration;

import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;

public class MoveDesire {
    double maxDistance   = 100;
    Duration maxDuration = Duration.millis(500);

    double targetX = 0;
    double targetY = 0;
    double fromX   = 0;
    double fromY   = 0;
    double rotateDegree = 0;

    public void setDesire(double fromX, double fromY, double toX, double toY) {
        targetX = toX;
        targetY = toY;
        this.fromX = fromX;
        this.fromY = fromY;
    }

    public void setDesire(double fromX, double fromY, double toX, double toY, double rotateDegree) {
        setDesire(fromX, fromY, toX, toY);
        this.rotateDegree = rotateDegree;
    }

    public void apply(ImageView toMove) {
        Rotate rotate = new Rotate(0, 0, 0);
        toMove.getTransforms().add(rotate);

        // Animate the rotation
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(rotate.angleProperty(), 0),
                new KeyValue(toMove.layoutXProperty(), fromX),
                new KeyValue(toMove.layoutYProperty(), fromY)
            ),
            new KeyFrame(getDuration(),
                new KeyValue(rotate.angleProperty(), rotateDegree),
                new KeyValue(toMove.layoutXProperty(), targetX),
                new KeyValue(toMove.layoutYProperty(), targetY)
            )
        );
        timeline.play();
    }

    private Duration getDuration() {
        double diffX = targetX - fromX;
        double diffY = targetY - fromY;
        double distance = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

        return Duration.millis(maxDuration.toMillis() * distance / maxDistance);
    }

}
