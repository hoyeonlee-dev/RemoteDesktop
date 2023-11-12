package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.connection.Session;
import kr.ac.hansung.remoteDesktop.connection.server.AudioServer;
import kr.ac.hansung.remoteDesktop.connection.server.ControlServer;
import kr.ac.hansung.remoteDesktop.connection.server.SessionManager;
import kr.ac.hansung.remoteDesktop.connection.server.VideoServer;
import kr.ac.hansung.remoteDesktop.screenCapture.DXGIScreenCapture;
import kr.ac.hansung.remoteDesktop.screenCapture.GDIScreenCapture;
import kr.ac.hansung.remoteDesktop.screenCapture.IScreenCapture;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;
import kr.ac.hansung.remoteDesktop.window.event.StopStreamingOnClose;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RemoteHostWindow implements IRDPWindow, Runnable {
    static long captureTime = 0;
    int captureMode = 0;
    private final JFrame hostWindow;

    private boolean shouldStop;

    public RemoteHostWindow(String title) throws HeadlessException {
        hostWindow = new JFrame(title);
        hostWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        hostWindow.addWindowListener(new StopStreamingOnClose(this));
        hostWindow.setLayout(new BorderLayout());
        hostWindow.setSize(1920, 1080);
        hostWindow.setLocationRelativeTo(null);
        shouldStop = false;
        hostWindow.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                var k = e.getKeyChar();
                if (k == KeyEvent.VK_1) {
                    captureMode = 0;
                }
                if (k == KeyEvent.VK_1) {
                    captureMode = 1;
                }
            }
        });
    }

    public static void main(String[] args) {
        new Thread(new RemoteHostWindow("원격 호스트")).start();


    }

    @Override
    public void showClient() {
        hostWindow.setVisible(true);
    }

    @Override
    public void hideClient() {
        hostWindow.setVisible(false);
    }

    @Override
    public void stopWindowAndService() {
        shouldStop = true;
    }

    @Override
    public void add(Component component) {

    }

    @Override
    public void add(Component component, Object constraints) {
        hostWindow.add(component, constraints);
    }

    @Override
    public void run() {
        IScreenCapture gdiCapture = new GDIScreenCapture(1920, 1080);
        IScreenCapture dxgiCapture = new DXGIScreenCapture(1920, 1080);

        var remoteScreen = new RemoteScreen(1920, 1080);
        add(remoteScreen, BorderLayout.CENTER);

        SessionManager sessionManager = new SessionManager();
        //@TODO 나중에 HOST ENABLED인 상태에서만 동작해야 함
        new Thread(new AudioServer(sessionManager, Session.AUDIO_PORT)).start();
        new Thread(new VideoServer(sessionManager, Session.VIDEO_PORT)).start();
        new Thread(new ControlServer(sessionManager, Session.CONTROL_PORT)).start();

        showClient();
        while (!shouldStop) {
            IScreenCapture iScreenCapture = null;
            switch (captureMode) {
                case 0:
                    iScreenCapture = dxgiCapture;
                    break;
                case 1:
                    iScreenCapture = gdiCapture;
                    break;
                default:
                    throw new RuntimeException("존재하지 않는 캡처모드입니다.");
            }
            iScreenCapture.doCapture();
            var buffer = iScreenCapture.getFrameBuffer();
            var sessions = sessionManager.getSessions();
            for (var p : sessions) {
                p.getValue().sendVideo(buffer);
            }
//            TODO: 오디오 캡처 구현하기
//                  구현한 뒤에 이 주석을 풀 것!
//            for (var p : sessions) {
//                p.getValue().sendAudio(buffer);
//            }
            remoteScreen.setImage(buffer);
            remoteScreen.repaint();
        }
    }
}
