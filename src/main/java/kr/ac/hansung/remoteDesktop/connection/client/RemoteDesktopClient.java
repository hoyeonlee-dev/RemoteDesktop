package kr.ac.hansung.remoteDesktop.connection.client;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import kr.ac.hansung.remoteDesktop.window.CustomKeyMessage;
import kr.ac.hansung.remoteDesktop.window.CustomMouseMessage;

public class RemoteDesktopClient {
    public static void main(String[] args) {
        
        try {
        	String serverIP = "127.0.0.1"; 
            int serverPort = 12345;

            Socket serverSocket = new Socket(serverIP, serverPort);
            System.out.println("서버에 연결되었습니다.");

            ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());

            // 키보드와 마우스 이벤트를 전송하는 메서드 호출
            sendKeyEvent(outputStream, KeyEvent.VK_A, true); // 키를 누름
            sendKeyEvent(outputStream, KeyEvent.VK_A, false); // 키를 뗌
            sendMouseMoveEvent(outputStream, 100, 200); // 마우스 이동
            sendMouseClickEvent(outputStream, 1, 300, 400); // 마우스 클릭

            // 클라이언트 소켓과 출력 스트림을 닫음
            outputStream.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 키 이벤트를 전송하는 메서드
    private static void sendKeyEvent(ObjectOutputStream outputStream, int keyCode, boolean isPressed) throws IOException {
        CustomKeyMessage keyMessage = new CustomKeyMessage(keyCode, isPressed);
        outputStream.writeObject(keyMessage);
    }

    // 마우스 이동 이벤트를 전송하는 메서드
    private static void sendMouseMoveEvent(ObjectOutputStream outputStream, int x, int y) throws IOException {
        CustomMouseMessage mouseMessage = new CustomMouseMessage(x, y, false);
        outputStream.writeObject(mouseMessage);
    }

    // 마우스 클릭 이벤트를 전송하는 메서드
    private static void sendMouseClickEvent(ObjectOutputStream outputStream, int button, int x, int y) throws IOException {
        CustomMouseMessage mouseMessage = new CustomMouseMessage(x, y, true);
        outputStream.writeObject(mouseMessage);
    }
}