package kr.ac.hansung.remoteDesktop.window;

import connection.Session;
import connection.server.AudioServer;
import connection.server.ControlServer;
import connection.server.SessionManager;
import connection.server.VideoServer;
import kr.ac.hansung.remoteDesktop.screenCapture.DXGIScreenCapture;
import kr.ac.hansung.remoteDesktop.screenCapture.GDIScreenCapture;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;
import kr.ac.hansung.remoteDesktop.window.event.StopStreamingOnClose;

import javax.swing.*;
import java.awt.*;

public class RemoteHostWindow implements IRDPWindow, Runnable {
    static long captureTime = 0;
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
    public void makeItStop() {
        shouldStop = true;
    }

    @Override
    public void add(Component component, Object constraints) {
        hostWindow.add(component, constraints);
    }

    @Override
    public void run() {
        var gdiCapture = new GDIScreenCapture(1920, 1080);
        var dxgiCapture = new DXGIScreenCapture(1920, 1080);

        var remoteScreen = new RemoteScreen(1920, 1080);
        add(remoteScreen, BorderLayout.CENTER);

        SessionManager sessionManager = new SessionManager();
        //@TODO 나중에 HOST ENABLED인 상태에서만 동작해야 함
        new Thread(new AudioServer(sessionManager, Session.AUDIO_PORT)).start();
        new Thread(new VideoServer(sessionManager, Session.VIDEO_PORT)).start();
        new Thread(new ControlServer(sessionManager, Session.CONTROL_PORT)).start();

        showClient();
        while (!shouldStop) {
            dxgiCapture.createBufferedImage();
            var buffer = dxgiCapture.getFrameBuffer();
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
