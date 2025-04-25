package server;
import model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import connection.Connection;

public class PlayerHandler implements Runnable {
    public PlayerID playerId;
    public GameSession session;
    public Connection conn;
    public List<Tile> tiles;

    public PlayerHandler(PlayerID id, Connection userConn, GameSession game) {
        playerId = id;
        conn = userConn;
        session = game;
        // store tiles to later support new tiles gathering
        tiles = new ArrayList<Tile>();
    }
    @Override
    public void run() {
        String clientMessage;
        try {
            while ((clientMessage = conn.recieveString()) != null) {
                GameResponse response = session.processCommand(new GameResponse(clientMessage), tiles);
                conn.sendString(response.toString());
            }
            System.out.println("Finished handling");
        }
        catch (IOException e) {
            System.out.println("Player " + playerId + " has network issues! Terminating handling...");
            e.printStackTrace();
        }
    }
}