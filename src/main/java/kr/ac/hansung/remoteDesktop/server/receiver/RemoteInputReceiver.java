package kr.ac.hansung.remoteDesktop.server.receiver;

import kr.ac.hansung.remoteDesktop.Settings;
import kr.ac.hansung.remoteDesktop.exception.ConnectionClosedByClientException;
import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.KeyboardMessage;
import kr.ac.hansung.remoteDesktop.message.content.MouseClick;
import kr.ac.hansung.remoteDesktop.message.content.MousePosition;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * 클라이언트 측에서 받은 마우스 및 키 이벤트를 처리합니다.
 */
public class RemoteInputReceiver implements Closeable {
    private final Robot robot;
    ObjectInputStream objectInputStream;
    Runnable onCloseMessageReceived;
    private boolean isClosed;

    public RemoteInputReceiver(ObjectInputStream objectInputStream, Robot robot) {
        this.objectInputStream = objectInputStream;
        this.robot = robot;
        isClosed = false;
    }

    public Runnable getOnCloseMessageReceived() {
        return onCloseMessageReceived;
    }

    public void setOnCloseMessageReceived(Runnable onCloseMessageReceived) {
        this.onCloseMessageReceived = onCloseMessageReceived;
    }

    //사용자로부터 도착한 메시지를 해석하고 그에 대한 동작을 실행하는 메서드
    public void processInputMessage() {
        if (isClosed) return;
        try {
            var message = (RemoteMessage) objectInputStream.readObject();
            System.out.printf("Server : received %s\n", message.toString());
            switch (message.getType()) {
                case CONNECTION_CLOSED:
                    if (onCloseMessageReceived != null) {
                        onCloseMessageReceived.run();
                    }
                    throw new ConnectionClosedByClientException("");
                case MOUSE_POSITION:
                    processMouseMove((MousePosition) message.getData());
                    break;
                case MOUSE_CLICK:
                    processMouseClick((MouseClick) message.getData());
                    break;
                case KEYBOARD:
                    processKeyMessage((KeyboardMessage) message.getData());
                    break;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("개발상 문제입니다. 개발자에게 문의하세요");
        }
    }

    private void processMouseClick(MouseClick mouseClick) {
        if (!Settings.getInstance().isAllowClientInput()) return;
        if (mouseClick.getKeyCode() == MouseClick.LEFT_BUTTON) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
        if (mouseClick.getKeyCode() == MouseClick.RIGHT_BUTTON) {
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }
    }

    private void processMouseMove(MousePosition mouseMessage) {
        if (!Settings.getInstance().isAllowClientInput()) return;
        robot.mouseMove(mouseMessage.getX(), mouseMessage.getY());

        if (mouseMessage.isClick()) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private void processKeyMessage(KeyboardMessage keyMessage) {
        if (!Settings.getInstance().isAllowClientInput()) return;
        int keyCode = keyMessage.getKeyCode();
        boolean isPressed = keyMessage.isPressed();

        if (isPressed) {
            robot.keyPress(keyCode);
        } else {
            robot.keyRelease(keyCode);
        }
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }
}
