package server;
import model.*;
import java.io.*;

import connection.Connection;

public class PlayerHandler implements Runnable {
    public PlayerID playerId;
    public GameSession session;
    public Connection conn;

    public PlayerHandler(PlayerID id, Connection userConn, GameSession game) {
        playerId = id;
        conn = userConn;
        session = game;
    }
    @Override
    public void run() {
        String clientMessage;
        try {
            while ((clientMessage = conn.recieveString()) != null) {
                // first message byte - from which client message recieved
                GameResponse response = session.processCommand(playerId + clientMessage);
                conn.sendString(response.toString());
            }
        }
        catch (IOException e) {
            System.out.println("Player " + playerId + " has network issues! Terminating handling...");
            e.printStackTrace();
        }
    }
}