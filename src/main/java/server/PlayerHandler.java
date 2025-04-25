package server;
import model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import connection.Connection;

public class PlayerHandler implements Runnable {
    public SessionManager sessionProvider; // checks on session management logic
    public GameSession session; // checks on game tiles placement logic
    public Connection conn;
    public List<Tile> tiles;

    public PlayerHandler(Connection userConn, SessionManager manager) {
        conn = userConn;
        sessionProvider = manager;
        // store tiles to later support new tiles gathering
        tiles = new ArrayList<Tile>();
    }
    @Override
    public void run() {
        String clientMessage;
        try {
            while ((clientMessage = conn.recieveString()) != null) {
                GameResponse clientIncomming = new GameResponse(clientMessage);

                //we have two state for client, where its in the game or idle
                if (!inSession()) {
                    // ignoring any requests unless it sends join session
                    if (clientIncomming.getType() != ResponseType.JOIN_SESSION)
                        conn.sendString(new GameResponse(ResponseType.BAD_MOVE).toString());
                    else
                        session = sessionProvider.joinSession(conn);
                }

                // Once we are in a session -> handle domino logic requests
                GameResponse response = session.processCommand(conn, clientIncomming, tiles);
                conn.sendString(response.toString());
            }
        }
        catch (IOException e) {
            System.out.println("Player has network issues! Terminating handling...");
            e.printStackTrace();
        }
    }

    public void joinSession(GameSession session) {
        this.session = session;
    }

    private boolean inSession() {
        if (session == null)
            return false;
        return true;
    }
}