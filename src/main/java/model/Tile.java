package model;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Tile {
    private int highVal = 7;
    private int lowVal = 7;

    private Position position = new Position(0, 0);
    private Dimension dimension = new Dimension(50, 100); // initial tiles size
    private double rotateDegree = 0;
    private boolean isVertical = false;
    private boolean isHighConnected = false;
    private boolean isLowConnected = false;

    static private String[] jsonNodes = {"highVal", "lowVal", "x", "y", "rotate", "dimH", "dimW"};

    public Tile() {}

    public Tile(int low, int high) {
        this.lowVal = low;
        this.highVal = high;
    }

    public Tile(String bytes) {
        try {
            JsonNode tile;
            if ((tile = checkFormatValid(bytes)) == null) {
                System.out.println(bytes);
                System.out.println("Input json does not contain required fileds for Tile.");
                return;
            }

            lowVal = tile.get("lowVal").asInt();
            highVal = tile.get("highVal").asInt();
            position = new Position(tile.get("x").asDouble(), tile.get("y").asDouble());
            rotateDegree = tile.get("rotate").asDouble();
            dimension = new Dimension(tile.get("dimH").asDouble(), tile.get("dimW").asDouble());
        }
        catch (JsonProcessingException e) {
            System.out.println("Unable to parse tile from json!");
            e.printStackTrace();
            return;
        }
    }

    public boolean isConnected(Tile nextTile) {
        System.out.println("Checking mutability between tiles:");
        System.out.println("Current tile - lowVal: " + lowVal + ", highVal: " + highVal + 
                         ", isLowConnected: " + isLowConnected + ", isHighConnected: " + isHighConnected);
        System.out.println("Next tile - lowVal: " + nextTile.getLowVal() + 
                         ", highVal: " + nextTile.getHighVal());

        if ((highVal == nextTile.getLowVal() || highVal == nextTile.getHighVal())
            && !isHighConnected) {
            System.out.println("Tiles are mutable through high side");
            return true;
        }

        if ((lowVal == nextTile.getLowVal() || lowVal == nextTile.getHighVal())
            && !isLowConnected) {
            System.out.println("Tiles are mutable through low side");
            return true;
        }

        System.out.println("Tiles are not mutable");
        return false;
    }

    // Getters
    public int getLowVal() { return lowVal; }
    public int getHighVal() { return highVal; }
    public double getX() { return position.getX(); }
    public double getY() { return position.getY(); }
    public double getWidth() { return dimension.getWidth(); }
    public double getLength() { return dimension.getLength(); }
    public double getSize() { return dimension.getWidth() * dimension.getLength(); }
    public boolean getHighConnected() { return isHighConnected; }
    public boolean getLowConnected() { return isLowConnected; }
    public boolean isVertical() { return isVertical; }
    public double getRotateDegree() { return rotateDegree; }

    // Setters
    public void setX(double x) { position.setX(x); }
    public void setY(double y) { position.setY(y); }
    public void setRotate(double degree) { this.rotateDegree = degree; }
    public void setHighConnected() { this.isHighConnected = true; }
    public void setLowConnected() { this.isLowConnected = true; }
    public void setWidth(double width) { dimension.setWidth(width); }
    public void setLength(double length) { dimension.setLength(length); }
    public void setVertical(boolean vertical) { this.isVertical = vertical; }

    @Override
    public String toString() {
        ObjectNode obj = JsonNodeFactory.instance.objectNode()
                        .put("highVal", highVal)
                        .put("lowVal", lowVal)
                        .put("x", position.getX())
                        .put("y", position.getY())
                        .put("rotate", rotateDegree)
                        .put("dimH", dimension.getLength())
                        .put("dimW", dimension.getWidth())
                        .put("isHighConnected", getHighConnected())
                        .put("isLowConnected", getLowConnected());
        return obj.toString();
    }

    public JsonNode toJsonNode() {
        ObjectNode obj = JsonNodeFactory.instance.objectNode()
                            .put("highVal", highVal)
                            .put("lowVal", lowVal)
                            .put("x", position.getX())
                            .put("y", position.getY())
                            .put("rotate", rotateDegree)
                            .put("dimH", dimension.getLength())
                            .put("dimW", dimension.getWidth())
                            .put("isHighConnected", getHighConnected())
                            .put("isLowConnected", getLowConnected());
        return obj;
    }

    static public JsonNode checkFormatValid(String bytes) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tile = mapper.readTree(bytes);

        for (String nodeName : jsonNodes) {
            if (tile.get(nodeName) == null)
                return null;
        }
        return tile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Tile toCompare = (Tile)obj;

        if ((toCompare.getLowVal() == lowVal && toCompare.getHighVal() == highVal) ||
            (toCompare.getLowVal() == highVal && toCompare.getHighVal() == lowVal))
                return true;
        return false;
    }
}
