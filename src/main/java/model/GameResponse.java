package model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GameResponse {
    private ResponseType type;
    private Update update = new Update();
    private Tile tile = new Tile();
    private Status status;

    // needs to validate json inpput
    static private final String[] jsonNodes = {"status", "type", "tile", "update"};

    public GameResponse(ResponseType type) {
        this.type = type;
        status = Status.OK;
    }

    public GameResponse(ResponseType type, Status status) {
        this.type = type;
        this.status = status;
    }

    // parse json from read bytes and init fields
    public GameResponse(String readBytes) {
        try {
            type = ResponseType.UNKNOWN;
            status = Status.OK;

            JsonNode response;
            if ((response = checkFormatValid(readBytes)) == null) {
                System.out.println("Input json does not containt required fields for GameResponse");
                return;
            }
            // read json if all fields are presented
            type = ResponseType.values()[response.get("type").asInt()];
            status = Status.values()[response.get("status").asInt()];
            update = new Update(response.get("update").toString());
            tile = new Tile(response.get("tile").toString());
        }
        catch (JsonProcessingException e) {
            System.out.println("Unable to process bytes to json: <" + readBytes + ">");
            e.printStackTrace();
        }
    }

    public void setUpdate(double resize, double deltaX, double deltaY) {
        update = new Update(resize, deltaX, deltaY);
    }

    public void setTile(Tile placeTile) {
        tile = placeTile;
    }

    @Override
    public String toString() {
        ObjectNode response = JsonNodeFactory.instance.objectNode()
                                .put("type", type.ordinal())
                                .put("status", status.ordinal());

        response.set("tile", tile.toJsonNode());
        response.set("update", update.toJsonNode());

        return response.toString();
    }

    public ResponseType getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public Tile getTile() {
        return tile;
    }

    public Update getUpdate() {
        return update;
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
