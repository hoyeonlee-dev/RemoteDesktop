package kr.ac.hansung.remoteDesktop.connection.server;


import kr.ac.hansung.remoteDesktop.Settings;
import kr.ac.hansung.remoteDesktop.connection.ConnectionType;
import kr.ac.hansung.remoteDesktop.network.message.AuthMessage;

import java.io.*;
import java.net.Socket;

public class ControlServer extends Server {
    public ControlServer(SessionManager sessionManager, int port) {
        super(sessionManager, port);
        connectionType = ConnectionType.CONTROL;
    }

    @Override
    public String getSessionID(Socket socket) {
        try {
            var sessionID = Long.toString(System.currentTimeMillis());
            if (!Settings.password.isEmpty()) {
                if (!passwordAuthentication(socket, sessionID)) return null;
            } else {
                var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(new AuthMessage(AuthMessage.Type.PASSWORD_NOT_REQUIRED, sessionID));
            }
            return sessionID;
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean passwordAuthentication(Socket socket, String sessionID) throws IOException {
        var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        var inputStreamReader  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        objectOutputStream.writeObject(new AuthMessage(AuthMessage.Type.PASSWORD_REQUIRED, sessionID));
        objectOutputStream.flush();
        int count = 0;
        for (int i = 0; i <= 3; i++) {
            if (!inputStreamReader.readLine().equals(Settings.password)) {
                if (++count >= 3) {
                    objectOutputStream.writeObject(new AuthMessage(AuthMessage.Type.CONNECTION_RESET, ""));
                    objectOutputStream.flush();
                    return false;
                } else {
                    objectOutputStream.writeObject(new AuthMessage(AuthMessage.Type.PASSWORD_WRONG, ""));
                    objectOutputStream.flush();
                }
            } else {
                objectOutputStream.writeObject(new AuthMessage(AuthMessage.Type.ACCEPTED, sessionID));
                objectOutputStream.flush();
                break;
            }
        }
        return true;
    }
}
