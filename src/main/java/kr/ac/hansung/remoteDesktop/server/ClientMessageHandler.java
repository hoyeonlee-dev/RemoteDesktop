package kr.ac.hansung.remoteDesktop.server;

import kr.ac.hansung.remoteDesktop.server.session.ServerSession;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.awt.*;

/**
 * 서버가 클라이언트와 메시지를 주고 받는데 사용하는 클래스
 */
public class ClientMessageHandler implements Runnable {
    private final ServerSession serverSession;
    private final SessionManager sessionManager;

    public ClientMessageHandler(SessionManager sessionManager, ServerSession serverSession) {
        this.sessionManager = sessionManager;
        this.serverSession = serverSession;
    }

    @Override
    public void run() {
        var receiver = serverSession.getRemoteInputReceiver();
        // 클라이언트가 종료 메시지를 전송했을 때
        serverSession.getRemoteInputReceiver().setOnCloseMessageReceived(() -> {
            serverSession.close();
            sessionManager.removeSession(serverSession.getSessionID());
        });
        while (!serverSession.isClosed()) {
            receiver.processInputMessage();
            Point mousePosition = MouseInfo.getPointerInfo().getLocation();
            serverSession.getMessageSender().sendMousePosition(mousePosition.x, mousePosition.y);
        }
    }
}
