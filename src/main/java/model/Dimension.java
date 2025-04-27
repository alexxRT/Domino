package model;

public class Dimension {
    private double width;
    private double length;

    public Dimension(double width, double length) {
        this.width = width;
        this.length = length;
    }

    public double getWidth() { return width; }
    public double getLength() { return length; }
    public void setWidth(double width) { this.width = width; }
    public void setLength(double length) { this.length = length; }
}
