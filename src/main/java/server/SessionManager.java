package server;

import java.util.ArrayList;
import java.util.*;
import connection.Connection;

public class SessionManager {
    private static ArrayList<GameSession> sessions = new ArrayList<>();

    public GameSession joinSession(Connection newPLayer) {
        if (sessions.size() == 0) {
            return addCreateSession(newPLayer);
        }

        GameSession lastSession = sessions.get(sessions.size() - 1);
        if (!lastSession.addPlayer(newPLayer)) {
            return addCreateSession(newPLayer);
        }
        return lastSession;
    }

    private GameSession addCreateSession(Connection addPlayer) {
        GameSession session = new GameSession();
        session.addPlayer(addPlayer);
        sessions.add(session);

        return session;
    }

    public static void closeAllSessions() {
        System.out.println("Closing all active game sessions...");
        for (GameSession session : sessions) {
            session.close();
        }
    }
}

