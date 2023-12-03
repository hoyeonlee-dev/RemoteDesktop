package kr.ac.hansung.remoteDesktop.server.receiver;

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
    Runnable          onCloseMessageReceived;
    private boolean isClosed;

    public RemoteInputReceiver(ObjectInputStream objectInputStream, Robot robot) {
        this.objectInputStream = objectInputStream;
        this.robot             = robot;
        isClosed               = false;
    }

    public Runnable getOnCloseMessageReceived() {
        return onCloseMessageReceived;
    }

    public void setOnCloseMessageReceived(Runnable onCloseMessageReceived) {
        this.onCloseMessageReceived = onCloseMessageReceived;
    }

    public void processInputMessage() {
        if (isClosed) return;
        try {
            var message = (RemoteMessage) objectInputStream.readObject();
            switch (message.getType()) {
                case CONNECTION_CLOSED:
                    if (onCloseMessageReceived != null)
                        onCloseMessageReceived.run();
                    break;
                case MOUSE_POSITION:
                    MousePosition mousePosition = (MousePosition) message.getData();
                    System.out.printf("mouse move : x : %d  y : %d Click %b\n", mousePosition.getX(), mousePosition.getY(), mousePosition.isClick());
                    processMouseMove(mousePosition);
                    break;
                case MOUSE_CLICK:
                    MouseClick mouseClick = (MouseClick) message.getData();
                    System.out.printf("mouse click : code : %s  Click %b\n", mouseClick.getKeyCode() == MouseClick.RIGHT_BUTTON ? "Right" : "Left", mouseClick.isPressed());
                    processMouseClick(mouseClick);
                    break;
                case KEYBOARD:
                    KeyboardMessage keyboardMessage = (KeyboardMessage) message.getData();
                    System.out.printf("KeyPressed : KeyCode : %d, isPressed : %b\n", keyboardMessage.getKeyCode(), keyboardMessage.isPressed());
                    processKeyMessage(keyboardMessage);
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    private void processMouseClick(MouseClick mouseClick) {
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
        robot.mouseMove(mouseMessage.getX(), mouseMessage.getY());

        if (mouseMessage.isClick()) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private void processKeyMessage(KeyboardMessage keyMessage) {
        int     keyCode   = keyMessage.getKeyCode();
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
