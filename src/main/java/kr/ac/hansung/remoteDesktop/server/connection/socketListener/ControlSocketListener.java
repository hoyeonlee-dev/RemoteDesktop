package kr.ac.hansung.remoteDesktop.server.connection.socketListener;


import kr.ac.hansung.remoteDesktop.server.session.ConnectionType;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.net.Socket;

public class ControlSocketListener extends SocketListener {
    public ControlSocketListener(SessionManager sessionManager, int port) {
        super(sessionManager, port);
        connectionType = ConnectionType.CONTROL;
    }

    @Override
    public String getSessionID(Socket socket) {
        return Long.toString(System.currentTimeMillis());
    }
}
