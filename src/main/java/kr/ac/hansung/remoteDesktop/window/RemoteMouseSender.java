package kr.ac.hansung.remoteDesktop.window;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RemoteMouseSender {
    private final Socket socket;
    private final ObjectOutputStream outputStream;

    public RemoteMouseSender(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public void sendMouseMessage(int x, int y, boolean isClick) throws IOException {
        CustomMouseMessage mouseMessage = new CustomMouseMessage(x, y, isClick);
        outputStream.writeObject(mouseMessage);
    }

    public void sendKeyMessage(int keyCode, boolean isPressed) throws IOException {
        CustomKeyMessage keyMessage = new CustomKeyMessage(keyCode, isPressed);
        outputStream.writeObject(keyMessage);
    }

    public void close() {
        try {
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
