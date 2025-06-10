package server;

import java.util.ArrayList;
import java.util.*;
import connection.Connection;
import java.util.concurrent.*;
import model.*;
import java.io.*;



public class SessionManager {
    // SESSION_ID = i (sessions[i])
    private ArrayList<GameSession> sessions = new ArrayList<>();
    private LinkedBlockingDeque<Response> clientResponses = new LinkedBlockingDeque<>();

    private List<PlayerHandler> players = new ArrayList<>();
    private ResponseHandler backend;

    public SessionManager() {
        backend = new ResponseHandler();
        Thread backendThread = new Thread(backend);

        backend.setThread(backendThread);
        backendThread.start();
    }

    public void closeAllSessions() {
        System.out.println("Closing all active game sessions...");
        for (GameSession session: sessions) {
            session.close();
        }
    }

    public void startNewClient(Connection client) {
        PlayerHandler newConnection = new PlayerHandler(client);
        Thread clientThread = new Thread(newConnection);
        newConnection.setThread(clientThread);

        players.add(newConnection);
        clientThread.start();
    }

    private GameSession addCreateSession(Connection addPlayer) {
        GameSession session = new GameSession();
        session.addPlayer(addPlayer);
        sessions.add(session);

        return session;
    }

    private class PlayerHandler implements Runnable {
        private Connection userEndpoint;
        private Thread serviceThread;

        public PlayerHandler(Connection userConn) {
            this.userEndpoint = userConn;
        }
        @Override
        public void run() {
            String clientMessage;
            try {
                while ((clientMessage = userEndpoint.recieveString()) != null) {
                    GameResponse clientIncomming = new GameResponse(clientMessage);
                    clientResponses.add(new Response(userEndpoint, clientIncomming));
                }
            }
            catch (IOException e) {
                System.out.println("Player has network issues! Terminating handling...");
                e.printStackTrace();
            }
        }

        public void setThread(Thread service) {
            serviceThread = serviceThread;
        }
    }

    private class ResponseHandler implements Runnable {
        private Thread serviceThread;

        public void setThread(Thread service) {
            serviceThread = service;
        }

        @Override
        public void run() {
            try {
                while(true) {
                    Response response = clientResponses.take();

                    GameResponse gameResponse = response.getBody();
                    Connection client = response.getConnection();

                    // need to create or add in existing session
                    if (gameResponse.getType() == ResponseType.JOIN_SESSION) {
                        int sessionID = gameResponse.getSessionID();
                        if (sessionID >= sessions.size() || sessionID < 0 ||
                            !sessions.get(sessionID).checkVacant()) {
                            sessionID = sessions.size();
                            GameSession newSession = addCreateSession(client);
                        } else { // add simply in existing session
                            sessions.get(sessionID).addPlayer(client);
                        }
                        // send here ON_JOIN with updated session ID
                        ArrayList<Response> sessionJoined = sessions.get(sessionID).processCommand(response);
                        for (Response res: sessionJoined) {
                            res.getBody().setSessionID(sessionID);
                            res.getConnection().sendString(res.getBody().toString());
                        }
                    }
                    else {
                        int sessionID = gameResponse.getSessionID();
                        GameSession session = sessions.get(sessionID);

                        ArrayList<Response> processedCmd = session.processCommand(response);
                        for (Response res: processedCmd)
                            res.getConnection().sendString(res.getBody().toString());
                    }
                }
            } catch (Exception shitHapppend) {
                System.out.println("Shit happened! Finishing response handler! Server is idle");
                shitHapppend.printStackTrace();
            }
        }
    }
}

