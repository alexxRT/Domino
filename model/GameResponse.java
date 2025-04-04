package model;

import java.util.ArrayList;
import java.util.List;

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
        StringBuilder response = new StringBuilder();
        response.append("Move [f:");

        switch (type) {
            case BAD_MOVE:
                response.append("BAD_MOVE](");
                break;
            case PLACE_MOVE:
                response.append("PLACE_MOVE](");
                break;
            case UPDATE_MOVE:
                response.append("UPDATE_MOVE](");
                break;
            case RESIZE:
                response.append("RESIZE](");
                break;
            default:
                response.append("UNKNOWN]()");
        }

        for (String tileInfo : updateTiles) {
            response.append(tileInfo);
        }
        response.append(")");

        return response.toString();
    }
}