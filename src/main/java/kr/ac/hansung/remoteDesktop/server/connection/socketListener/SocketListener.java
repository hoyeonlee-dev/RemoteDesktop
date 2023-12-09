package kr.ac.hansung.remoteDesktop.server.connection.socketListener;


import kr.ac.hansung.remoteDesktop.server.session.ConnectionType;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class SocketListener implements Runnable {
    private final int port;

    private final SessionManager sessionManager;
    protected ConnectionType connectionType;
    private ServerSocket serverSocket;

    public SocketListener(SessionManager sessionManager, int port) {
        this.port = port;
        this.sessionManager = sessionManager;
    }

    public abstract String getSessionID(Socket socket);

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                var accepted = serverSocket.accept();
                new Thread(new AcceptedSocketHandler(accepted)).start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void stopServer() throws IOException {
        if (!serverSocket.isClosed()) serverSocket.close();
    }

    class AcceptedSocketHandler implements Runnable {

        private final Socket accepted;

        public AcceptedSocketHandler(Socket accepted) {
            this.accepted = accepted;
        }

        @Override
        public void run() {
            var sessionID = getSessionID(accepted);
            if (sessionID == null) {
                try {
                    accepted.close();
                } catch (IOException e) {
                    System.err.println("Socket closed");
                }
                return;
            }
            if (!sessionManager.attachSocket(sessionID, accepted, connectionType)) {
                System.err.println("Attach에 실패했습니다.");
            }
        }
    }

}
