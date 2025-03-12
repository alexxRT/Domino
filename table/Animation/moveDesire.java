package Animation;

import javafx.util.Duration;

import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point3D;
import javafx.scene.image.ImageView;

public class moveDesire {
    private TranslateTransition placeMove  = new TranslateTransition();
    private RotateTransition rotateMove    = new RotateTransition();
    private ParallelTransition placeDesire = new ParallelTransition();

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
        placeMove.setNode(toMove);
        placeMove.setByX(targetX - fromX);
        placeMove.setByY(targetY - fromY);

        rotateMove.setNode(toMove);
        rotateMove.setAxis(new Point3D(0, 0,1));
        rotateMove.setByAngle(rotateDegree);

        Duration playTime = getDuration();
        rotateMove.setDuration(playTime);
        placeMove.setDuration(playTime);

        placeDesire.getChildren().addAll(rotateMove, placeMove);
        placeDesire.play();
    }

    private Duration getDuration() {
        double diffX = targetX - fromX;
        double diffY = targetY - fromY;
        double distance = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

        return Duration.millis(maxDuration.toMillis() * distance / maxDistance);
    }

}
