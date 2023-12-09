package kr.ac.hansung.remoteDesktop.server.sender;

import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.ConnectionClosed;
import kr.ac.hansung.remoteDesktop.message.content.MousePosition;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MessageSender implements Closeable {

    private final ObjectOutputStream controlObjectOutputStream;
    private boolean isClosed;

    public MessageSender(ObjectOutputStream objectOutputStream) throws IOException {
        controlObjectOutputStream = objectOutputStream;
        isClosed = false;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void sendMousePosition(int x, int y) {
        try {
            controlObjectOutputStream.writeObject(
                    new RemoteMessage(RemoteMessage.Type.MOUSE_POSITION, new MousePosition(x, y, false)));
            controlObjectOutputStream.flush();
        } catch (IOException e) {
        }
    }

    public void sendCloseMessage(String message) throws IOException {
        controlObjectOutputStream.writeObject(
                new RemoteMessage(RemoteMessage.Type.CONNECTION_CLOSED, new ConnectionClosed(message)));
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }
}
