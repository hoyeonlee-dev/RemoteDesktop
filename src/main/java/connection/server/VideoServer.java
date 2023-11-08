package connection.server;

import connection.ConnectionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class VideoServer extends Server {
    public VideoServer(SessionManager sessionManager, int port) {
        super(sessionManager, port);
        connectionType = ConnectionType.VIDEO;
    }

    @Override
    public String getSessionID(Socket socket) {
        try {
            var bw = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return bw.readLine();
        } catch (IOException e) {
            return null;
        }

    }
}
