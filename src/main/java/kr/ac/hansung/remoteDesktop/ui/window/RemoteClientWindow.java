package kr.ac.hansung.remoteDesktop.ui.window;

import kr.ac.hansung.remoteDesktop.client.connection.ClientSession;
import kr.ac.hansung.remoteDesktop.exception.ConnectionFailureException;
import kr.ac.hansung.remoteDesktop.ui.component.RemoteScreen;
import kr.ac.hansung.remoteDesktop.ui.window.dialog.AskPasswordDialog;
import kr.ac.hansung.remoteDesktop.ui.window.event.ClientFileDropHandlerImpl;
import kr.ac.hansung.remoteDesktop.ui.window.event.ClientWindowListener;
import kr.ac.hansung.remoteDesktop.ui.window.event.FileDropHandler;
import kr.ac.hansung.remoteDesktop.ui.window.event.FileDropListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 사용자와 상호작용하는 원격데스크톱의 클라이언트 화면
 */
public class RemoteClientWindow implements Runnable {
    private final String address;
    private final RemoteScreen remoteScreen;
    byte[] buffer = null;
    FileDropHandler fileDropHandler;
    private JFrame clientWindow;
    private boolean shouldStop;
    private ClientSession clientSession;

    public RemoteClientWindow(String title, String address) {
        remoteScreen = createWindow(title);
        this.address = address;
        shouldStop = false;

        buffer = new byte[1920 * 1080 * 4];
    }

    public void setTitle(String title) {
        if (clientWindow != null) {
            clientWindow.setTitle(title);
        }
    }

    private RemoteScreen createWindow(String title) {
        final RemoteScreen remoteScreen;
        clientWindow = new JFrame(title);
        clientWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        clientWindow.setLayout(new GridLayout(1, 1));
        clientWindow.setSize(1920, 1080);
        clientWindow.setLocationRelativeTo(null);

        remoteScreen = new RemoteScreen(1920, 1080);
        clientWindow.add(remoteScreen);

        var clientWindowListener = new ClientWindowListener();
        clientWindowListener.setWindowClosedRunnable(java.util.List.of(clientWindow::dispose, this::closeSession));
        clientWindow.addWindowListener(clientWindowListener);

        clientWindow.setVisible(true);
        clientWindow.pack();

        clientWindow.addWindowStateListener(e -> {
            if (e.getNewState() == WindowEvent.WINDOW_CLOSING) shouldStop = true;
        });
        return remoteScreen;
    }

    public void showClient() {
        clientWindow.setVisible(true);
    }

    public void hideClient() {
        clientWindow.setVisible(false);
    }

    public void add(Component component, Object constraints) {
        clientWindow.add(component, constraints);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void updateRemoteScreen() throws IOException {
        remoteScreen.setImage(ImageIO.read(new ByteArrayInputStream(getBuffer())));
        remoteScreen.repaint();
    }

    // 서버에서 비밀번호를 요구할 때 사용자에게 암호를 묻습니다.
    public String askPassword(AskPasswordDialog.Type type) {
        var askPasswordWindow = new AskPasswordDialog(type, clientWindow);
        askPasswordWindow.setPosition(100, 100);
        askPasswordWindow.setDialogSize(400, 300);
        AtomicReference<String> password = new AtomicReference<>("");
        askPasswordWindow.setSubmit(l -> {
            password.set(askPasswordWindow.getPassword());
            askPasswordWindow.dispose();
        });
        askPasswordWindow.setCancel(r -> {
            password.set(null);
            askPasswordWindow.dispose();
        });
        askPasswordWindow.start();
        return password.get();
    }

    public void closeSession() {
        if (clientSession != null) {
            try {
                clientSession.close();
            } catch (IOException e) {
            }
        }
    }

    private ClientSession createClientSession() throws ConnectionFailureException {
        var clientSession = ClientSession.Factory.createClientSession(address, this::askPassword);
        var receiver = clientSession.getMessageReceiver();
        var mouseSender = clientSession.getMouseSender();
        var keySender = clientSession.getKeySender();

        clientWindow.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    super.keyPressed(e);
                    if (e.getKeyCode() == 0) return;
                    keySender.sendKeyPress(e.getKeyCode());
                } catch (IOException ex) {
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    super.keyReleased(e);
                    if (e.getKeyCode() == 0) return;
                    keySender.sendKeyRelease(e.getKeyCode());
                } catch (IOException ex) {
                }
            }
        });

        clientWindow.getContentPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int button = e.getButton();
                boolean pressed = true;
                try {
                    mouseSender.sendMouseClick(button, pressed);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 클라이언트 화면에서 사용자가 마우스를 움직였을 때 할 동작
        clientWindow.getContentPane().addMouseMotionListener(new MouseAdapter() {
            private long lastSent = System.nanoTime();

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                try {
                    long now = System.nanoTime();
                    //너무 자주 업데이트하면 많은 메시지 전송으로 정상적인 원격 데스크톱 사용이 불가함
                    if ((now - lastSent) / 1000_000 > 30) {
                        mouseSender.sendMouseMove(e.getX(), e.getY());
                        System.out.printf("\r\n%d %d", e.getX(), e.getY());
                        lastSent = now;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 창이 닫힐 때 할 동작들
        receiver.setOnWindowCloseReceived(() -> {
            try {
                clientSession.close();
            } catch (IOException ignored) {
            } finally {
                clientWindow.dispose();
            }
        });

        // 서버의 마우스 위치가 도착했을 때 할 동작들
        receiver.setOnMouseMessageReceived(r -> {
            remoteScreen.setMouseX(r.getX());
            remoteScreen.setMouseY(r.getY());
            remoteScreen.repaint();
        });

        var fileDropListener = new FileDropListener();
        fileDropHandler = new ClientFileDropHandlerImpl(clientSession);
        fileDropListener.addFileDropAction(fileDropHandler);
        new DropTarget(clientWindow, fileDropListener);

        System.out.println("연결했습니다.");
        return clientSession;
    }

    @Override
    public void run() {
        try { // 서버에 접속을 요청하는 부분
            clientSession = createClientSession();
        } catch (IOException e) { // 암호 인증에 실패했거나 타임아웃에 걸려 연결하지 못한 경우
            System.out.printf("클라이언트 윈도우를 종료합니다. %s", e.getMessage());
            clientWindow.dispose();
            return;
        }

        //연결에 성공했고 원격 데스크톱 세션을 시작할 수 있음
        clientWindow.setTitle(address);
        var receiver = clientSession.getMessageReceiver();

        // 서버로부터의 메시지를 처리하는 스레드
        new Thread(() -> {
            while (!clientSession.isClosed()) {
                try {
                    receiver.handleServerMessage();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }
        }).start();

        showClient();

        // 이미지를 수신하고 화면을 업데이트 하는 부분
        try {
            while (!clientSession.isClosed()) {
                var videoReceiver = clientSession.getVideoReceiver();
                int len = videoReceiver.receiveVideo(getBuffer());
                int STOP = -100;
                if (len == STOP) break;
                updateRemoteScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
