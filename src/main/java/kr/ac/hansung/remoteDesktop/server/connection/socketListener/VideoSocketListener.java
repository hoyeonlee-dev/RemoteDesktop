package kr.ac.hansung.remoteDesktop.server.connection.socketListener;

import kr.ac.hansung.remoteDesktop.server.session.ConnectionType;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

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
