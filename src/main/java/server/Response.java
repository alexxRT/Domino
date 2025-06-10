package server;

import connection.Connection;
import model.*;

public class Response {
    Connection clientEndpoint;
    GameResponse clientBody;

    public Response(Connection endpoint, GameResponse response) {
        clientEndpoint = endpoint;
        clientBody = response;
    }

    public Connection getConnection() { return clientEndpoint; }

    public GameResponse getBody() { return clientBody; }

    public void setBody(GameResponse body) { clientBody = body; }

}
