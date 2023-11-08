package kr.ac.hansung.remoteDesktop.connection.server;

import kr.ac.hansung.remoteDesktop.connection.ConnectionType;

import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, ServerSession> sessions;

    public SessionManager() {
        sessions = new ConcurrentHashMap<>();
    }

    public Set<Map.Entry<String, ServerSession>> getSessions() {

        return sessions.entrySet();
    }

    public void attachSession(String sessionID, ServerSession serverSession) {
        sessions.put(sessionID, serverSession);
    }

    public boolean attachSocket(String sessionID, Socket socket, ConnectionType type) {
        var session = sessions.get(sessionID);
        if (session == null && type == ConnectionType.CONTROL) {
            sessions.put(sessionID,
                    new ServerSession(null, null, socket)
            );
            return true;
        }
        if (session == null) return false;
        switch (type) {
            case VIDEO -> session.setVideoSocket(socket);
            case AUDIO -> session.setAudioSocket(socket);
            case CONTROL -> session.setControlSocket(socket);
        }
        return true;
    }

    public void remove(String sessionID) {
        var session = sessions.get(sessionID);
        if (session == null) return;
        sessions.remove(sessionID);
    }
}
