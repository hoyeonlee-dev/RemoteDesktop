package kr.ac.hansung.remoteDesktop.connection.server;


import kr.ac.hansung.remoteDesktop.connection.ConnectionType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ControlServer extends Server {
    public ControlServer(SessionManager sessionManager, int port) {
        super(sessionManager, port);
        connectionType = ConnectionType.CONTROL;
    }

    @Override
    public String getSessionID(Socket socket) {
        try {
            var bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write(toString());
            bw.newLine();
            bw.flush();
            return toString();
        } catch (IOException e) {
            return null;
        }
    }
}
