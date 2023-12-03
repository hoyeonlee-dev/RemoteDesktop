package kr.ac.hansung.remoteDesktop.ui.window.example;

import kr.ac.hansung.remoteDesktop.message.content.KeyboardMessage;
import kr.ac.hansung.remoteDesktop.message.content.MousePosition;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteDesktopServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("서버가 시작되었습니다. 클라이언트의 연결을 기다립니다.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트가 연결되었습니다.");

                // 클라이언트와의 통신을 처리하는 스레드 시작
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // ObjectInputStream을 생성하여 클라이언트로부터 데이터를 수신
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

                // 클라이언트로부터 받은 데이터를 처리하는 메서드 호출
                processClientInput(inputStream);

                // 클라이언트 소켓과 입력 스트림을 닫음
                inputStream.close();
                clientSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // 클라이언트로부터 받은 데이터를 처리하는 메서드
        private void processClientInput(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            while (true) {
                Object receivedObject = inputStream.readObject();
                if (receivedObject instanceof MousePosition) {
                    processMouseMessage((MousePosition) receivedObject);
                } else if (receivedObject instanceof KeyboardMessage) {
                    processKeyMessage((KeyboardMessage) receivedObject);
                }
            }
        }

        // 마우스 메시지를 처리하는 메서드
        private void processMouseMessage(MousePosition mouseMessage) {
            System.out.println("Received Mouse Message: x=" + mouseMessage.getX() + ", y=" + mouseMessage.getY() + ", click=" + mouseMessage.isClick());
        }

        // 키 메시지를 처리하는 메서드
        private void processKeyMessage(KeyboardMessage keyMessage) {
            System.out.println("Received Key Message: keyCode=" + keyMessage.getKeyCode() + ", pressed=" + keyMessage.isPressed());
        }
    }
}