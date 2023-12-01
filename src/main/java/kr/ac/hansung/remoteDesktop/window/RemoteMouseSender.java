package kr.ac.hansung.remoteDesktop.window;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import kr.ac.hansung.remoteDesktop.network.message.KeyEventInfo;
import kr.ac.hansung.remoteDesktop.network.message.MouseEventInfo;

public class RemoteMouseSender {

    private Socket socket;
    private ObjectOutputStream outputStream;

    public RemoteMouseSender(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public void sendMouseMove(int x, int y) throws IOException {
        RemoteMessage message = new RemoteMessage(RemoteMessageType.MOUSE_MOVE, new Point(x, y));
        outputStream.writeObject(message);
    }

    public void sendMouseClick(int button, boolean pressed) throws IOException {
        MouseEventInfo clickInfo = new MouseEventInfo(MouseEventInfo.MOUSE_CLICK, button, pressed);
        outputStream.writeObject(clickInfo);
        outputStream.flush();
    }

    public void sendKeyEvent(int keyCode, boolean keyPress) throws IOException {
        RemoteMessage message = new RemoteMessage(RemoteMessageType.KEY_EVENT, new KeyEventInfo(keyCode, keyPress));
        outputStream.writeObject(message);
        outputStream.flush();
    }
}


