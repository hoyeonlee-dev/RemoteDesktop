package kr.ac.hansung.remoteDesktop.window;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.ObjectInputStream;
import java.net.Socket;

public class RemoteInputProcessor implements Runnable {
    private final Socket clientSocket;
    private final Robot robot;

    public RemoteInputProcessor(Socket clientSocket, Robot robot) {
        this.clientSocket = clientSocket;
        this.robot = robot;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Object receivedObject = inputStream.readObject();
                if (receivedObject instanceof CustomMouseMessage) {
                    processMouseMessage((CustomMouseMessage) receivedObject);
                } else if (receivedObject instanceof CustomKeyMessage) {
                    processKeyMessage((CustomKeyMessage) receivedObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMouseMessage(CustomMouseMessage mouseMessage) {
        robot.mouseMove(mouseMessage.getX(), mouseMessage.getY());

        if (mouseMessage.isClick()) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private void processKeyMessage(CustomKeyMessage keyMessage) {
        int keyCode = keyMessage.getKeyCode();
        boolean isPressed = keyMessage.isPressed();

        if (isPressed) {
            robot.keyPress(keyCode);
        } else {
            robot.keyRelease(keyCode);
        }
    }
}