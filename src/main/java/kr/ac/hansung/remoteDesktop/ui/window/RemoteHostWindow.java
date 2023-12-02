package kr.ac.hansung.remoteDesktop.ui.window;

import kr.ac.hansung.remoteDesktop.server.connection.socketListener.ControlSocketListener;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.FileSocketListener;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.SocketListener;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.VideoSocketListener;
import kr.ac.hansung.remoteDesktop.server.session.ServerSession;
import kr.ac.hansung.remoteDesktop.server.session.Session;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;
import kr.ac.hansung.remoteDesktop.ui.window.event.HostWindowListener;
import kr.ac.hansung.remoteDesktop.ui.window.event.OnFileReceivedListener;
import kr.ac.hansung.remoteDesktop.util.screenCapture.DXGIScreenCapture;
import kr.ac.hansung.remoteDesktop.util.screenCapture.DisplaySetting;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RemoteHostWindow implements IRDPWindow {
    public static final  int               EXPECTED_REFRESH_RATE = 20;
    private static final DXGIScreenCapture dxgiCapture           = new DXGIScreenCapture(1920, 1080);
    private static final DisplaySetting    displaySettings       = new DisplaySetting();
    private final        JFrame            hostWindow;
    Thread         thread     = null;
    DisplaySetting displaySetting;
    long           lastSecond = 0;
    int            count      = 0;
    private byte[]             savedBuffer = null;
    private SessionManager     sessionManager;
    private SocketListener     videoSocketListener;
    private SocketListener     controlSocketListener;
    private int                width       = 600;
    private int                height      = 500;
    private boolean            shouldStop;
    private FileSocketListener fileSocketListener;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startListeningServer();

            waitUntilClientConnection(); // 클라이언트가 접속할 때까지 대기

            displaySettings.backupDisplaySettings();

            initScreenCapture();
            showClient(); //클라이언트가 접속하면 화면을 보여줌

            for (var s : sessionManager.getSessions()) {
                new Thread(() -> {
                    var session  = s.getValue();
                    var receiver = session.getRemoteInputReceiver();
                    while (!session.isClosed()) {
                        receiver.processInputMessage();
                        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
                        session.getMessageSender().sendMousePosition(mousePosition.x, mousePosition.y);
                    }
                }).start();
            }
            var sessions = sessionManager.getSessions();


            long lastSent = System.nanoTime();


            //원격 세션
            while (thread == Thread.currentThread()) {
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
            }

        }
    };

    public RemoteHostWindow(String title) throws HeadlessException {
        hostWindow = new JFrame(title);
        hostWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        hostWindow.setLayout(new BorderLayout());
        hostWindow.setSize(getWidth(), getHeight());
        hostWindow.setLocationRelativeTo(null);

        var hostWindowListener = new HostWindowListener();
        hostWindowListener.setWindowCloseRunnable(java.util.List.of(this::stop, this::closeSessions, this::deInitScreenCapture, this::stopListeningSockets));
        hostWindow.addWindowListener(hostWindowListener);

        shouldStop = false;
    }

    public void start() {
        thread = new Thread(runnable);
        thread.start();
    }

    public void stop() {
        thread = null;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        hostWindow.setSize(width, getHeight());
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        hostWindow.setSize(getWidth(), height);
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
        sessionManager        = new SessionManager();
        videoSocketListener   = new VideoSocketListener(sessionManager, Session.VIDEO_PORT);
        controlSocketListener = new ControlSocketListener(sessionManager, Session.CONTROL_PORT);

        fileSocketListener = new FileSocketListener(new OnFileReceivedListener(hostWindow, sessionManager));

        new Thread(videoSocketListener).start();
        new Thread(controlSocketListener).start();
        new Thread(fileSocketListener).start();
    }

    private void closeSessions() {
        for (var session : sessionManager.getSessions()) {
            session.getValue().close();
        }
        fileSocketListener.close();
    }

    /**
     * 호스트 리스닝 소켓을 종료합니다.
     */
    private void stopListeningSockets() {
        try {
            controlSocketListener.stopServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            videoSocketListener.stopServer();
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

    /**
     * 스크린 캡처를 초기화합니다.
     */
    private void initScreenCapture() {
        // native resize call
        displaySetting = new DisplaySetting();
        displaySetting.backupDisplaySettings();
        // displaySetting.resize(1920, 1080, 60);
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

    public void sendMousePosition(Set<Map.Entry<String, ServerSession>> sessions, int x, int y) {
        for (var p : sessions) {
            p.getValue().getMessageSender().sendMousePosition(x, y);
        }
    }

    public void sendNoUpdate(Set<Map.Entry<String, ServerSession>> sessions) {
        for (var p : sessions) {
            p.getValue().getVideoSender().sendNoUpdate();
        }
    }

    public void sendVideo(Set<Map.Entry<String, ServerSession>> sessions) {
        for (var p : sessions) {
            p.getValue().getVideoSender().sendVideo(savedBuffer, 1920, 1080);
        }
    }

    private boolean wasImageUpdated(byte[] byteArray1, byte[] byteArray2) {
        return Arrays.compare(byteArray1, byteArray2) != 0;
    }

    private void updateImage(byte[] capturedImage) {
        if (capturedImage.length > savedBuffer.length) savedBuffer = new byte[capturedImage.length];
        System.arraycopy(capturedImage, 0, savedBuffer, 0, capturedImage.length);
    }
}
