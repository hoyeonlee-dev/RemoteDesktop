package kr.ac.hansung.remoteDesktop.client.receiver;

import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.MousePosition;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.function.Consumer;

public class MessageReceiver implements Closeable {
    private final ObjectInputStream controlObjectInputStream;

    private Consumer<MousePosition> onMouseMessageReceived;
    private Runnable                onWindowCloseReceived;
    private boolean                 isClosed;

    public MessageReceiver(ObjectInputStream controlObjectInputStream) {
        this.controlObjectInputStream = controlObjectInputStream;
        isClosed                      = false;
    }

    public Consumer<MousePosition> getOnMouseMessageReceived() {
        return onMouseMessageReceived;
    }

    public void setOnMouseMessageReceived(Consumer<MousePosition> onMouseMessageReceived) {
        this.onMouseMessageReceived = onMouseMessageReceived;
    }

    public Runnable getOnWindowCloseReceived() {
        return onWindowCloseReceived;
    }

    public void setOnWindowCloseReceived(Runnable onWindowCloseReceived) {
        this.onWindowCloseReceived = onWindowCloseReceived;
    }

    public void handleServerMessage() throws IOException, ClassNotFoundException {
        try {
            var obj = controlObjectInputStream.readObject();
            if (obj instanceof RemoteMessage message) {
                if (message.getType() == RemoteMessage.Type.CONNECTION_CLOSED && onWindowCloseReceived != null)
                    onWindowCloseReceived.run();
                else if (message.getType() == RemoteMessage.Type.MOUSE_POSITION) {
                    if (onMouseMessageReceived != null)
                        onMouseMessageReceived.accept((MousePosition) message.getData());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            if (e instanceof SocketException) throw e;
        }
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        if (controlObjectInputStream != null)
            controlObjectInputStream.close();
    }
}
