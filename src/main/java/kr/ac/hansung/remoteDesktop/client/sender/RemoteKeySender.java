package kr.ac.hansung.remoteDesktop.client.sender;

import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.KeyboardMessage;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 클라이언트 측에서 서버로 키 이벤트를 전송하는 메서드를 제공합니다
 */
public class RemoteKeySender implements Closeable {
    private ObjectOutputStream objectOutputStream;
    private boolean            isClosed;

    public RemoteKeySender(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
        isClosed                = false;
    }

    public void sendKeyPress(int keyCode) throws IOException {
        if (isClosed) return;
        RemoteMessage message = new RemoteMessage(RemoteMessage.Type.KEYBOARD, new KeyboardMessage(keyCode, true));
        objectOutputStream.writeObject(message);
    }

    public void sendKeyRelease(int keyCode) throws IOException {
        if (isClosed) return;
        RemoteMessage message = new RemoteMessage(RemoteMessage.Type.KEYBOARD, new KeyboardMessage(keyCode, false));
        objectOutputStream.writeObject(message);
    }

    @Override
    public void close() throws IOException {
        objectOutputStream = null;
        isClosed           = true;
    }
}