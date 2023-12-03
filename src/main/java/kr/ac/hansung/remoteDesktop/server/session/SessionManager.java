package kr.ac.hansung.remoteDesktop.server.session;

import kr.ac.hansung.remoteDesktop.exception.ConnectionFailureException;

import java.io.IOException;
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

    public void removeSession(String key) {
        sessions.remove(key);
    }

    public void attachSession(String sessionID, ServerSession serverSession) {
        sessions.put(sessionID, serverSession);
    }

    public boolean attachSocket(String sessionID, Socket socket, ConnectionType type) {
        var session = sessions.get(sessionID);
        try {
            if (session == null && type == ConnectionType.CONTROL) {
                session = new ServerSession(sessionID);
                session.attachControlSocket(socket);
                if (!session.passwordAuthentication())
                    throw new ConnectionFailureException("Client Socket 초기화에 실패했습니다.");
                sessions.put(sessionID, session);
                return true;
            } else if (session != null && type == ConnectionType.VIDEO) {
                session.attachVideoSocket(socket);
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            if (session != null) {
                try {
                    session.getMessageSender().sendCloseMessage("호스트에서 연결을 처리하던 중 문제가 발생했습니다.");
                } catch (IOException ex) {
                    System.out.println(ex.getLocalizedMessage());
                    ex.printStackTrace();
                } finally {
                    sessions.remove(sessionID);
                }
            }
        }
        return false;
    }

    public void remove(String sessionID) {
        var session = sessions.get(sessionID);
        if (session == null) return;
        sessions.remove(sessionID);
    }
}
