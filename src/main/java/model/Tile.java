package model;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Tile {
    private int leftVal;
    private int rightVal;
    private Position position;
    private Dimension dimension;
    private double rotateDegree;
    private boolean isVertical = false;
    private boolean mutedRight = false;
    private boolean mutedLeft = false;

    public Tile(int left, int right) {
        this.leftVal = left;
        this.rightVal = right;
        this.position = new Position(0, 0);
        this.dimension = new Dimension(0, 0);
    }

    public boolean areMutable(Tile nextTile) {
        if ((rightVal == nextTile.getLeftVal() || rightVal == nextTile.getRightVal())
            && !mutedRight)
            return true;

        if ((leftVal == nextTile.getLeftVal() || leftVal == nextTile.getRightVal())
            && !mutedLeft)
            return true;

        return false;
    }

    // Getters
    public int getLeftVal() { return leftVal; }
    public int getRightVal() { return rightVal; }
    public double getX() { return position.getX(); }
    public double getY() { return position.getY(); }
    public double getWidth() { return dimension.getWidth(); }
    public double getLength() { return dimension.getLength(); }
    public double getSize() { return dimension.getWidth() * dimension.getLength(); }
    public boolean getMutedRight() { return mutedRight; }
    public boolean getMutedLeft() { return mutedLeft; }
    public boolean isVertical() { return isVertical; }
    public double getRotateDegree() { return rotateDegree; }

    // Setters
    public void setX(double x) { position.setX(x); }
    public void setY(double y) { position.setY(y); }
    public void setRotate(double degree) { this.rotateDegree = degree; }
    public void setMutedRight() { this.mutedRight = true; }
    public void setMutedLeft() { this.mutedLeft = true; }
    public void setWidth(double width) { dimension.setWidth(width); }
    public void setLength(double length) { dimension.setLength(length); }
    public void setVertical(boolean vertical) { this.isVertical = vertical; }

    @Override
    public String toString() {
        ObjectNode obj = JsonNodeFactory.instance.objectNode()
                        .put("rval", rightVal)
                        .put("lval", leftVal)
                        .put("x", position.getX())
                        .put("y", position.getY());
        return obj.toString();
    }
}