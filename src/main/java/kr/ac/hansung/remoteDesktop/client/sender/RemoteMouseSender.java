package kr.ac.hansung.remoteDesktop.client.sender;

import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.KeyEventInfo;
import kr.ac.hansung.remoteDesktop.message.content.MouseClick;
import kr.ac.hansung.remoteDesktop.message.content.MousePosition;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 클라이언트 측에서 서버로 마우스 이벤트를 전송하는 메서드를 제공합니다.
 */
public class RemoteMouseSender implements Closeable {

    private ObjectOutputStream objectOutputStream;
    private boolean            isClosed;

    public RemoteMouseSender(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
        isClosed                = false;
    }

    public void sendMouseMove(int x, int y) throws IOException {
        if (isClosed) return;
        RemoteMessage message = new RemoteMessage(RemoteMessage.Type.MOUSE_POSITION, new MousePosition(x, y, false));
        objectOutputStream.writeObject(message);
    }

    public void sendMouseClick(int button, boolean pressed) throws IOException {
        if (isClosed) return;
        RemoteMessage clickInfo = new RemoteMessage(RemoteMessage.Type.MOUSE_CLICK, new MouseClick(button, pressed));
        objectOutputStream.writeObject(clickInfo);
        objectOutputStream.flush();
    }

    /**
     * @param keyCode
     * @param keyPress
     * @throws IOException
     * @deprecated
     */
    public void sendKeyEvent(int keyCode, boolean keyPress) throws IOException {
        if (isClosed) return;
        RemoteMessage message = new RemoteMessage(RemoteMessage.Type.MOUSE_POSITION, new KeyEventInfo(keyCode, keyPress));
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        objectOutputStream = null;
        isClosed           = true;
    }
}


