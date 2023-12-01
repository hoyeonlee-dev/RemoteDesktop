package kr.ac.hansung.remoteDesktop.window;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RemoteKeySender {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;

    public RemoteKeySender(Socket socket) throws IOException {
        this.socket = socket;
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public void sendKeyPress(int keyCode) throws IOException {
        RemoteMessage message = new RemoteMessage(RemoteMessageType.KEY_PRESS, keyCode);
        objectOutputStream.writeObject(message);
    }

    public void sendKeyRelease(int keyCode) throws IOException {
        RemoteMessage message = new RemoteMessage(RemoteMessageType.KEY_RELEASE, keyCode);
        objectOutputStream.writeObject(message);
    }
}