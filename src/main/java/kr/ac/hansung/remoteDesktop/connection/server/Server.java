package kr.ac.hansung.remoteDesktop.connection.server;


import kr.ac.hansung.remoteDesktop.connection.ConnectionType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Server implements Runnable {
    private final int port;
    private final SessionManager sessionManager;

    protected ConnectionType connectionType;

    public Server(SessionManager sessionManager, int port) {
        this.port = port;
        this.sessionManager = sessionManager;
    }

    public abstract String getSessionID(Socket socket);

    @Override
    public void run() {
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                var accepted = serverSocket.accept();
                new Thread(() -> {
                    var sessionID = getSessionID(accepted);
                    if (sessionID == null) {
                        try {
                            accepted.close();
                        } catch (IOException e) {
                            System.err.println("Socket closed");
                        }
                    }
                    if (sessionManager.attachSocket(sessionID, accepted, connectionType)) {
                        System.err.println("Attach에 실패했습니다.");
                    }
                }).start();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
