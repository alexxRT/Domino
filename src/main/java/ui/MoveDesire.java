package ui;

import javafx.util.Duration;

import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import java.util.*;

public class MoveDesire {
    double maxDistance   = 100;
    Duration maxDuration = Duration.millis(500);

    double targetX = 0;
    double targetY = 0;
    double fromX   = 0;
    double fromY   = 0;
    double rotateDegree = 0;
    boolean swap = false;

    public void setDesire(double fromX, double fromY, double toX, double toY) {
        targetX = toX;
        targetY = toY;
        this.fromX = fromX;
        this.fromY = fromY;
    }

    public void setDesire(double fromX, double fromY, double toX, double toY, double rotateDegree, boolean swap) {
        setDesire(fromX, fromY, toX, toY);
        this.rotateDegree = rotateDegree;
        this.swap = swap;
    }

    public void apply(ImageView toMove) {
        // disable transforms before apply
        toMove.getTransforms().clear();

        ArrayList<KeyValue> frameEventsStart = new ArrayList<>();
        ArrayList<KeyValue> frameEventsEnd = new ArrayList<>();

        Rotate rotateLeftTop = new Rotate(0, 0, 0);
        toMove.getTransforms().add(rotateLeftTop);

        frameEventsStart.add(new KeyValue(rotateLeftTop.angleProperty(), 0));
        frameEventsEnd.add(new KeyValue(rotateLeftTop.angleProperty(), rotateDegree));

        if (swap) {
            double axisX = toMove.getFitWidth() / 2;
            double axisY = toMove.getFitHeight() / 2;

            System.out.println("axisX = " + axisX);
            System.out.println("axisY = " + axisY);

            Rotate rotateCenter = new Rotate(0, axisX, axisY);
            toMove.getTransforms().add(rotateCenter);

            frameEventsStart.add(new KeyValue(rotateCenter.angleProperty(), 0));
            frameEventsEnd.add(new KeyValue(rotateCenter.angleProperty(), 180));

            System.out.println("Add swap 180 for tile");
        }

        frameEventsStart.add(new KeyValue(toMove.layoutXProperty(), fromX));
        frameEventsStart.add(new KeyValue(toMove.layoutYProperty(), fromY));

        frameEventsEnd.add(new KeyValue(toMove.layoutXProperty(), targetX));
        frameEventsEnd.add(new KeyValue(toMove.layoutYProperty(), targetY));

        // Animate the rotation
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                frameEventsStart.toArray(new KeyValue[0])
            ),
            new KeyFrame(getDuration(),
                frameEventsEnd.toArray(new KeyValue[0])
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
