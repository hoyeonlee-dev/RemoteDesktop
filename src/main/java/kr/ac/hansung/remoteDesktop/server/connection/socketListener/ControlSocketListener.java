package kr.ac.hansung.remoteDesktop.server.connection.socketListener;


import kr.ac.hansung.remoteDesktop.server.session.ConnectionType;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.net.Socket;

/**
 * 클라이언트의 반복적인 제어 소켓 접속을 처리하기 위한 스레드(SocketListener가 Runnable임)
 */
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
