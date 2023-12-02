package kr.ac.hansung.remoteDesktop.server.session;

import java.io.IOException;
import java.net.Socket;

public abstract class Session {
    public static int VIDEO_PORT = 1236;
    public static int CONTROL_PORT = 12340;
    public static int filePort;
    protected Socket videoSocket;
    protected Socket controlSocket;


    public Session(Socket videoSocket, Socket controlSocket) {
        this.videoSocket = videoSocket;
        this.controlSocket = controlSocket;
    }

    public Socket getVideoSocket() {
        return videoSocket;
    }

    public void setVideoSocket(Socket videoSocket) {
        this.videoSocket = videoSocket;
    }

    public Socket getControlSocket() {
        return controlSocket;
    }

    public void setControlSocket(Socket controlSocket) {
        this.controlSocket = controlSocket;
    }

    public void closeSession() {
        try {
            if (videoSocket != null && !videoSocket.isClosed()) {
                videoSocket.close();
                videoSocket = null;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        try {
            if (controlSocket != null && !controlSocket.isClosed()) {
                controlSocket.close();
                controlSocket = null;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
