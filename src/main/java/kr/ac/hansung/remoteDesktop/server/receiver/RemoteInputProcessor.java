package kr.ac.hansung.remoteDesktop.server.receiver;

import kr.ac.hansung.remoteDesktop.message.content.KeyboardMessage;
import kr.ac.hansung.remoteDesktop.message.content.MousePosition;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * @deprecated Use {@link kr.ac.hansung.remoteDesktop.server.receiver.RemoteInputReceiver} instead
 */
public class RemoteInputProcessor implements Runnable {
    private final Socket clientSocket;
    private final Robot  robot;

    public RemoteInputProcessor(Socket clientSocket, Robot robot) {
        this.clientSocket = clientSocket;
        this.robot        = robot;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Object receivedObject = inputStream.readObject();
                if (receivedObject instanceof MousePosition) {
                    processMouseMessage((MousePosition) receivedObject);
                } else if (receivedObject instanceof KeyboardMessage) {
                    processKeyMessage((KeyboardMessage) receivedObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMouseMessage(MousePosition mouseMessage) {
        // 마우스 메시지 처리 로직 작성
        System.out.println("Received Mouse Message: x=" + mouseMessage.getX() + ", y=" + mouseMessage.getY() + ", click=" + mouseMessage.isClick());

        // 여기에서 좌표 변환 및 로봇 조작 로직 추가
        robot.mouseMove(mouseMessage.getX(), mouseMessage.getY());

        if (mouseMessage.isClick()) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private void processKeyMessage(KeyboardMessage keyMessage) {
        // 키 메시지 처리 로직 작성
        System.out.println("Received Key Message: keyCode=" + keyMessage.getKeyCode() + ", pressed=" + keyMessage.isPressed());

        // 여기에서 로봇 조작 로직 추가
        int     keyCode   = keyMessage.getKeyCode();
        boolean isPressed = keyMessage.isPressed();

        if (isPressed) {
            robot.keyPress(keyCode);
        } else {
            robot.keyRelease(keyCode);
        }
    }
}