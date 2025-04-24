package model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GameResponse {
    private ResponseType type;
    private List<String> updateTiles;

    public GameResponse(ResponseType type) {
        this.type = type;
        this.updateTiles = new ArrayList<>();
    }

    public void addUpdateTile(Tile tile) {
        updateTiles.add(tile.toString());
    }

    @Override
    public String toString() {
        ObjectNode response = JsonNodeFactory.instance.objectNode()
                                .put("type", type.ordinal());

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = response.putArray("tiles");

        try {
            for (String tileInfo : updateTiles) {
                arrayNode.add(mapper.readTree(tileInfo));
            }
        }
        catch (JsonProcessingException e) {
            System.out.println("Unable to properly add json for all update tiles!");
            e.printStackTrace();
        }
        return response.toString();
    }
}