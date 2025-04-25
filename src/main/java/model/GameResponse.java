package model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GameResponse {
    private ResponseType type;
    private List<String> updateTiles;

    // needs to validate json inpput
    static private final String[] jsonNodes = {"status", "type", "tiles"};

    public GameResponse(ResponseType type) {
        this.type = type;
        this.updateTiles = new ArrayList<>();
    }

    // parse json from read bytes and init fields
    public GameResponse(String readBytes) {
        try {
            type = ResponseType.BAD_MOVE;
            updateTiles = new ArrayList<>();

            JsonNode response;
            if ((response = checkFormatValid(readBytes)) == null) {
                System.out.println("Input json does not containt required fields for GameResponse");
                return;
            }
            // read json if all fields are presented
            type = ResponseType.values()[response.get("type").asInt()];
            for (JsonNode node : response.withArrayProperty("tiles"))
                updateTiles.add(node.toString());
        }
        catch (JsonProcessingException e) {
            System.out.println("Unable to process bytes to json: [" + readBytes + "]");
            e.printStackTrace();

            // constructing default BAD_MOVE response
            type = ResponseType.BAD_MOVE;
            updateTiles = new ArrayList<>();
        }
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

    public ResponseType getType() {
        return type;
    }

    // get tile from update at concrete index
    // throws exception if no such tile
    public Tile getTile(int index) throws IndexOutOfBoundsException {
        return new Tile(updateTiles.get(index));
    }

    static public JsonNode checkFormatValid(String bytes) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode response = mapper.readTree(bytes);

        // check that all filds are presented
        for (String nodeName : jsonNodes) {
            if (response.get(nodeName) == null)
                return null;
        }
        return response;
    }
}