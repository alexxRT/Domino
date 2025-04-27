package model;

public class GameBoard {
    private Dimension dimension;
    private Position position;

    public GameBoard(double width, double height, Position position) {
        this.dimension = new Dimension(width, height);
        this.position = position;
    }

    // Getters
    public double getWidth() { return dimension.getWidth(); }
    public double getHeight() { return dimension.getLength(); }
    public Position getPosition() { return position; }
    public Dimension getDimension() { return dimension; }

    // Setters
    public void setDimension(Dimension dimension) { this.dimension = dimension; }
    public void setPosition(Position position) { this.position = position; }
    public void setWidth(double width) { dimension.setWidth(width); }
    public void setHeight(double height) { dimension.setLength(height); }
}
