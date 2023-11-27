package kr.ac.hansung.remoteDesktop.window;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class RemoteControlFrame extends JPanel {
    private Socket serverSocket;
    private RemoteMouseSender remoteMouseSender;

    public RemoteControlFrame(Socket serverSocket) {
        this.serverSocket = serverSocket;

        try {
            remoteMouseSender = new RemoteMouseSender(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    remoteMouseSender.sendMouseMessage(e.getX(), e.getY(), true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                try {
                    remoteMouseSender.sendMouseMessage(e.getX(), e.getY(), false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                try {
                    remoteMouseSender.sendKeyMessage(e.getKeyCode(), true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                try {
                    remoteMouseSender.sendKeyMessage(e.getKeyCode(), false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        new Thread(() -> handleInputEvents()).start();
        new Thread(() -> updateRemoteScreen()).start();
    }

    private void updateRemoteScreen() {

    }

    private void handleInputEvents() {
        try {
            ObjectInputStream input = new ObjectInputStream(serverSocket.getInputStream());

            while (true) {
                // 서버에서 전송된 메시지를 읽어와서 처리
                RemoteMessage message = (RemoteMessage) input.readObject();
                switch (message.getType()) {
                    case INPUT_MODE:
                        // INPUT_MODE에 대한 처리
                        break;
                    case MOUSE_EVENT:
                        // 마우스 이벤트 처리
                        handleMouseEvent((CustomMouseMessage) message.getData());
                        break;
                    case KEY_EVENT:
                        // 키보드 이벤트 처리
                        handleKeyEvent((CustomKeyMessage) message.getData());
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleMouseEvent(CustomMouseMessage customMouseMessage) {
        // 마우스 이벤트 처리 로직 추가 
    }

    private void handleKeyEvent(CustomKeyMessage customKeyMessage) {
        // 키보드 이벤트 처리 로직 추가 
    }
}
