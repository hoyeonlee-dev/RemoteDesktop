package kr.ac.hansung.remoteDesktop.ui.window;

import kr.ac.hansung.remoteDesktop.server.connection.socketListener.ControlSocketListener;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.FileSocketListener;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.SocketListener;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.VideoSocketListener;
import kr.ac.hansung.remoteDesktop.server.session.ServerSession;
import kr.ac.hansung.remoteDesktop.server.session.Session;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;
import kr.ac.hansung.remoteDesktop.ui.window.event.ClientMessageHandler;
import kr.ac.hansung.remoteDesktop.ui.window.event.OnFileReceivedListener;
import kr.ac.hansung.remoteDesktop.util.screenCapture.DXGIScreenCapture;
import kr.ac.hansung.remoteDesktop.util.screenCapture.DisplaySetting;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RemoteHostDaemon {
    public static final int EXPECTED_REFRESH_RATE = 20;
    private static final DXGIScreenCapture dxgiCapture = new DXGIScreenCapture(1920, 1080);
    private static final DisplaySetting displaySettings = new DisplaySetting();
    private final int width = 600;
    private final SessionManager sessionManager;
    private final int height = 500;
    Thread thread = null;
    DisplaySetting displaySetting;
    long lastSecond = 0;
    int count = 0;
    private byte[] savedBuffer = null;
    // 스트리밍을 담당하는 스레드의 동작을 정의한 메서드
    Runnable mainLoop = new Runnable() {
        @Override
        public void run() {
            while (true) {
                waitUntilClientConnection(); // 클라이언트가 접속할 때까지 대기

                displaySettings.backupDisplaySettings();

                initScreenCapture();

                for (var s : sessionManager.getSessions()) {
                    var session = s.getValue();
                    if (session != null) {
                        new Thread(new ClientMessageHandler(sessionManager, session)).start();
                    }
                }
                var sessions = sessionManager.getSessions();


                long lastSent = System.nanoTime();


                //원격 세션
                while (sessionManager.getSessions().size() > 0) {
                    if (!canCaptureNextFrame(System.nanoTime(), lastSent)) continue;

                    if (!dxgiCapture.doCapture()) {
                        broadcastNoUpdate(sessions);
                        continue;
                    }

                    var capturedImage = dxgiCapture.getFrameBuffer();

                    if (savedBuffer == null) savedBuffer = new byte[capturedImage.length];

                    if (wasImageUpdated(capturedImage, savedBuffer)) {
                        updateImage(capturedImage);
                        broadcastVideo(sessions);
                        printStatics();
                    } else {
                        broadcastNoUpdate(sessions);
                    }

                    lastSent = System.nanoTime();
                }
            }
        }
    };
    private SocketListener videoSocketListener;
    private SocketListener controlSocketListener;
    private FileSocketListener fileSocketListener;
    private JFrame parentWindow;
    private boolean isListening;

    /**
     * 생성자
     */
    public RemoteHostDaemon() {
        sessionManager = new SessionManager();
        isListening = false;
        thread = new Thread(mainLoop);
        thread.start();
    }

    public void setParentWindow(JFrame parentWindow) {
        this.parentWindow = parentWindow;
    }

    /////////////////////////////////////////////////////////////
    //// region 리스닝 서버 조작
    /////////////////////////////////////////////////////////////
    public void start() {
        if (!isListening) {
            isListening = true;
            startListeningServer();
        }
    }

    public void stop() {
        stopSocketListeners();
    }

    private void startListeningServer() {
        videoSocketListener = new VideoSocketListener(sessionManager, Session.VIDEO_PORT);
        controlSocketListener = new ControlSocketListener(sessionManager, Session.CONTROL_PORT);

        fileSocketListener = new FileSocketListener(new OnFileReceivedListener(parentWindow, sessionManager));

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
    private void stopSocketListeners() {
        isListening = false;
        try {
            controlSocketListener.stopServer();
            controlSocketListener = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            videoSocketListener.stopServer();
            videoSocketListener = null;
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
            count = 0;
        }
    }

    /////////////////////////////////////////////////////////////
    //// endregion
    /////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////
    //// region 영상관련
    /////////////////////////////////////////////////////////////

    /**
     * 접속 중인 클라이언트에 영상을 전송
     *
     * @param sessions
     */
    public void broadcastVideo(Set<Map.Entry<String, ServerSession>> sessions) {
        for (var p : sessions) {
            var session = p.getValue();
            if (session.isClosed()) continue;
            session.getVideoSender().sendVideo(savedBuffer, 1920, 1080);
        }
    }

    /**
     * 영상의 변경사항이 없는 경우 그 사실을 클라이언트에게 전송
     *
     * @param sessions
     */
    public void broadcastNoUpdate(Set<Map.Entry<String, ServerSession>> sessions) {
        for (var p : sessions) {
            var session = p.getValue();
            if (session.isClosed()) continue;
            session.getVideoSender().sendNoUpdate();
        }
    }


    /**
     * 영상이 이전 프레임에서 업데이트 되었는지 확인합니다.
     *
     * @param byteArray1 영상을 담은 배열
     * @param byteArray2 영상을 담은 배열
     * @return 두 배열의 동일 여부
     */
    private boolean wasImageUpdated(byte[] byteArray1, byte[] byteArray2) {
        return Arrays.compare(byteArray1, byteArray2) != 0;
    }

    /**
     * 캡처한 이미지를 전송용 버퍼에 복사합니다.
     *
     * @param capturedImage 복사할 이미지
     */
    private void updateImage(byte[] capturedImage) {
        if (capturedImage.length > savedBuffer.length) savedBuffer = new byte[capturedImage.length];
        System.arraycopy(capturedImage, 0, savedBuffer, 0, capturedImage.length);
    }

    /////////////////////////////////////////////////////////////
    //// endregion
    /////////////////////////////////////////////////////////////
}
