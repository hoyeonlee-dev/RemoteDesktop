package kr.ac.hansung.remoteDesktop.server.connection.socketListener;

import kr.ac.hansung.remoteDesktop.server.session.ConnectionType;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 클라이언트의 반복적인 영상 소켓 접속을 처리하기 위한 스레드(SocketListener가 Runnable임)
 */
public class VideoSocketListener extends SocketListener {
    public VideoSocketListener(SessionManager sessionManager, int port) {
        super(sessionManager, port);
        connectionType = ConnectionType.VIDEO;
    }

    @Override
    public String getSessionID(Socket socket) {
        try {
            var bw = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var sessionID = bw.readLine();
            return sessionID;
        } catch (IOException e) {
            return null;
        }

    }
}
