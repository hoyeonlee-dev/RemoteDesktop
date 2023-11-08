package connection.server;


import connection.Session;

import java.io.IOException;
import java.net.Socket;

public class ServerSession extends Session {

    public ServerSession(Socket videoSocket, Socket audioSocket, Socket controlSocket) {
        super(videoSocket, audioSocket, controlSocket);
    }

    public boolean sendVideo(byte[] buffer) {
        if (videoSocket == null) return false;
        if (videoSocket.isClosed()) return false;

        try {
            var outputStream = videoSocket.getOutputStream();
            outputStream.write(buffer, 0, buffer.length);
            outputStream.flush();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean sendAudio(byte[] buffer) {
        if (audioSocket == null) return false;
        if (audioSocket.isClosed()) return false;
        try {
            audioSocket.getOutputStream().write(buffer);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
}
