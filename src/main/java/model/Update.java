package model;

import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;


public class Update {
    private double resizeCoeff = 0;
    private double translateX = 0;
    private double translateY = 0;

    private static final String[] jsonNodes = {"resize", "deltaX", "deltaY"};

    public Update() {};

    public Update(double resize, double transX, double transY) {
        resizeCoeff = resize;
        translateX = transX;
        translateY = transY;
    }

    public Update(String bytes) {
        try {
            JsonNode update;
            if ((update = checkFormatValid(bytes)) == null) {
                System.out.println("Input json does not contain required fileds for Tile.");
                return;
            }
            resizeCoeff = update.get("resize").asDouble();
            translateX = update.get("deltaX").asDouble();
            translateY = update.get("deltaY").asDouble();
        }
        catch (JsonProcessingException e) {
            System.out.println("Unable to parse tile from json!");
            e.printStackTrace();
            return;
        }
    }

    public double getResize() {
        return resizeCoeff;
    }

    public double getDeltaX() {
        return translateX;
    }

    public double getDeltaY() {
        return translateY;
    }

    @Override
    public String toString() {
        ObjectNode obj = JsonNodeFactory.instance.objectNode()
                            .put("resize", resizeCoeff)
                            .put("deltaX", translateX)
                            .put("deltaY", translateY);
        return obj.toString();
    }

    public JsonNode toJsonNode() {
        ObjectNode obj = JsonNodeFactory.instance.objectNode()
                                .put("resize", resizeCoeff)
                                .put("deltaX", translateX)
                                .put("deltaY", translateY);
        return obj;
    }

    static public JsonNode checkFormatValid(String bytes) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode upd = mapper.readTree(bytes);

        for (String nodeName : jsonNodes) {
            if (upd.get(nodeName) == null)
                return null;
        }
        return upd;
    }

}
