package connection;

import java.io.IOException;
import java.net.Socket;

public abstract class Session {
    public static int VIDEO_PORT = 1236;
    public static int AUDIO_PORT = 1235;
    public static int CONTROL_PORT = 12340;
    public static int filePort;
    protected Socket videoSocket;
    protected Socket audioSocket;
    protected Socket controlSocket;


    public Session(Socket videoSocket, Socket audioSocket, Socket controlSocket) {
        this.videoSocket = videoSocket;
        this.audioSocket = audioSocket;
        this.controlSocket = controlSocket;
    }

    public Socket getVideoSocket() {
        return videoSocket;
    }

    public void setVideoSocket(Socket videoSocket) {
        this.videoSocket = videoSocket;
    }

    public Socket getAudioSocket() {
        return audioSocket;
    }

    public void setAudioSocket(Socket audioSocket) {
        this.audioSocket = audioSocket;
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
            if (audioSocket != null && !audioSocket.isClosed()) {
                audioSocket.close();
                audioSocket = null;
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
