package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.connection.Session;
import kr.ac.hansung.remoteDesktop.connection.server.*;
import kr.ac.hansung.remoteDesktop.screenCapture.DXGIScreenCapture;
import kr.ac.hansung.remoteDesktop.screenCapture.DisplaySetting;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;
import kr.ac.hansung.remoteDesktop.window.event.OnFileReceivedListener;
import kr.ac.hansung.remoteDesktop.window.event.StopStreamingOnClose;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RemoteHostWindow implements IRDPWindow, Runnable {
    public static final int    EXPECTED_REFRESH_RATE = 20;
    private             JFrame hostWindow;

    private DXGIScreenCapture dxgiCapture;

    private byte[] savedBuffer = null;

    private SessionManager sessionManager;
    private FileServer     fileServer;
    private Server         videoServer;
    private Server         controlServer;


    private RemoteScreen remoteScreen = null;

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
    public void stopWindowAndService() {
        shouldStop = true;
    }

    @Override
    public void add(Component component) {
        hostWindow.add(component);
    }

    @Override
    public void add(Component component, Object constraints) {
        hostWindow.add(component, constraints);
    }

    private void startListeningServer() {
        if (remoteScreen == null) remoteScreen = new RemoteScreen(1920, 1080);
        add(remoteScreen, BorderLayout.CENTER);

        sessionManager = new SessionManager();
        videoServer    = new VideoServer(sessionManager, Session.VIDEO_PORT);
        controlServer  = new ControlServer(sessionManager, Session.CONTROL_PORT);

        fileServer = new FileServer(new OnFileReceivedListener(hostWindow, sessionManager));

        new Thread(videoServer).start();
        new Thread(controlServer).start();
        new Thread(fileServer).start();
    }


    /**
     * 호스트 리스닝 소켓을 종료합니다.
     */
    private void stopListeningSockets() {
        try {
            controlServer.stopServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            videoServer.stopServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 클라이언트가 접속할 때까지 대기합니다.
     */
    public void waitUntilClientConnection() {
        while (sessionManager.getSessions().size() == 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }

    DisplaySetting displaySetting;

    /**
     * 스크린 캡처를 초기화합니다.
     */
    private void initScreenCapture() {
        // native resize call
        displaySetting = new DisplaySetting();
        displaySetting.backDisplaySettings();
        displaySetting.resize(1920, 1080, 60);
        dxgiCapture = new DXGIScreenCapture(1920, 1080);
    }

    /**
     * 스크린 캡처를 중단한 뒤 해야할 것들을 진행합니다.
     */
    private void deInitScreenCapture() {
        // native resize call
        displaySetting.restore();

    }

    /**
     * 지난 캡처로부터 시간을 측정하여 새로 캡처할 것인지를 확인하는 메서드
     *
     * @param now  현재 시간(System.nanotime())
     * @param prev 이전 시간
     * @return 업데이트 가능 여부
     */
    private boolean canCaptureNextFrame(long now, long prev) {
        return ((now - prev) / 1_000_000) > EXPECTED_REFRESH_RATE;
    }

    void printStatics() {
        var diff = (double) (System.nanoTime() - lastSecond) / 1_000_000;
        if (diff > 1000) {
            System.out.printf("지난 %.3f초동안 %d 프레임 전송\r", diff, ++count);
            lastSecond = System.nanoTime();
            count      = 0;
        }
    }

    public void sendNoUpdate(Set<Map.Entry<String, ServerSession>> sessions) {
        for (var p : sessions) {
            p.getValue().sendNoUpdate();
        }
    }

    public void sendVideo(Set<Map.Entry<String, ServerSession>> sessions) {
        for (var p : sessions) {
            p.getValue().sendVideo(savedBuffer, 1920, 1080);
        }
    }

    private boolean wasImageUpdated(byte[] byteArray1, byte[] byteArray2) {
        return Arrays.compare(byteArray1, byteArray2) != 0;
    }

    private void updateImage(byte[] capturedImage) {
        if (capturedImage.length > savedBuffer.length) savedBuffer = new byte[capturedImage.length];
        System.arraycopy(capturedImage, 0, savedBuffer, 0, capturedImage.length);
    }

    private void drawImage() {
        remoteScreen.setImage(savedBuffer);
        remoteScreen.repaint();
    }

    long lastSecond = 0;
    int  count      = 0;

    @Override
    public void run() {
        startListeningServer();

        waitUntilClientConnection(); // 클라이언트가 접속할 때까지 대기

        initScreenCapture();

        showClient(); //클라이언트가 접속하면 화면을 보여줌

        var sessions = sessionManager.getSessions();

        new Thread(() -> {
            while (true) {
                for (var session : sessions) {
                    session.getValue().checkFileTransferRequest();
                }
            }
        }).start();

        long lastSent = System.nanoTime();


        //원격 세션
        while (System.nanoTime() != 0) {
            if (!canCaptureNextFrame(System.nanoTime(), lastSent)) continue;

            if (!dxgiCapture.doCapture()) continue;

            var capturedImage = dxgiCapture.getFrameBuffer();

            if (savedBuffer == null) savedBuffer = new byte[capturedImage.length];

            if (wasImageUpdated(capturedImage, savedBuffer)) {
                updateImage(capturedImage);
                sendVideo(sessions);
                printStatics();
            } else {
                sendNoUpdate(sessions);
            }

            lastSent = System.nanoTime();
            drawImage();
        }

        deInitScreenCapture();
        stopListeningSockets();
    }
}
