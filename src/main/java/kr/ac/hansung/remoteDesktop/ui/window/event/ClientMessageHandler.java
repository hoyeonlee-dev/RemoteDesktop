package kr.ac.hansung.remoteDesktop.ui.window.event;

import kr.ac.hansung.remoteDesktop.server.session.ServerSession;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.awt.*;

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
