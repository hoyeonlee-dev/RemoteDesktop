package kr.ac.hansung.remoteDesktop.ui.window.event;

import kr.ac.hansung.remoteDesktop.server.session.ServerSession;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.awt.*;

public class ClientMessageHandler implements Runnable {
    private final ServerSession  session;
    private final SessionManager sessionManager;

    public ClientMessageHandler(SessionManager sessionManager, ServerSession serverSession) {
        this.sessionManager = sessionManager;
        this.session        = serverSession;
    }

    @Override
    public void run() {
        var receiver = session.getRemoteInputReceiver();
        session.getRemoteInputReceiver().setOnCloseMessageReceived(() -> {
            session.close();
            sessionManager.removeSession(session.getSessionID());
        });
        while (!session.isClosed()) {
            receiver.processInputMessage();
            Point mousePosition = MouseInfo.getPointerInfo().getLocation();
            session.getMessageSender().sendMousePosition(mousePosition.x, mousePosition.y);
        }
    }
}
