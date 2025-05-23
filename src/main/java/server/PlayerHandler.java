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
    public List<Tile> tiles = new ArrayList<Tile>();  // store tiles to later support new tiles gathering

    public PlayerHandler(Connection userConn, SessionManager manager) {
        conn = userConn;
        sessionProvider = manager;
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
                        conn.sendString(new GameResponse(ResponseType.UNKNOWN, Status.AGAIN).toString());
                    else {
                        session = sessionProvider.joinSession(conn);
                        for (GameResponse onJoin : session.processCommand(conn, clientIncomming, tiles))
                            conn.sendString(onJoin.toString());
                    }
                }
                else {
                    // Once we are in a session -> handle domino logic requests
                    for (GameResponse response : session.processCommand(conn, clientIncomming, tiles))
                        conn.sendString(response.toString());
                }
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
