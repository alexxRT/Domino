package model;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Tile {
    private int leftVal = 7;
    private int rightVal = 7;
    private Position position = new Position(0, 0);
    private Dimension dimension = new Dimension(0, 0);
    private double rotateDegree = 0;
    private boolean isVertical = false;
    private boolean mutedRight = false;
    private boolean mutedLeft = false;

    static private String[] jsonNodes = {"rval", "lval", "x", "y", "rotate", "dimH", "dimW"};

    public Tile() {}

    public Tile(int left, int right) {
        this.leftVal = left;
        this.rightVal = right;
    }

    public Tile(String bytes) {
        try {
            JsonNode tile;
            if ((tile = checkFormatValid(bytes)) == null) {
                System.out.println("Input json does not contain required fileds for Tile.");
                return;
            }

            leftVal = tile.get("lval").asInt();
            rightVal = tile.get("rval").asInt();
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
                        .put("y", position.getY())
                        .put("rotate", rotateDegree)
                        .put("dimH", dimension.getLength())
                        .put("dimW", dimension.getWidth());
        return obj.toString();
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
}
