package kr.ac.hansung.remoteDesktop.connection.client;

import kr.ac.hansung.remoteDesktop.connection.Session;
import org.xerial.snappy.Snappy;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSession extends Session {
    private String sessionID;

    public ClientSession(String sessionID, Socket controlSocket) {
        this(null, null, controlSocket);
        this.sessionID = sessionID;
    }

    private ClientSession(Socket videoSocket, Socket audioSocket, Socket controlSocket) {
        super(videoSocket, audioSocket, controlSocket);
    }

    public boolean requestVideoSocket() {
        var host        = controlSocket.getInetAddress().getHostAddress();
        var videoSocket = new Socket();
        try {
            videoSocket.connect(new InetSocketAddress(InetAddress.getByName(host), Session.VIDEO_PORT));
            var writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(videoSocket.getOutputStream())));
            writer.println(sessionID);
            writer.flush();
            this.videoSocket = videoSocket;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            if (videoSocket.isConnected()) {
                try {
                    videoSocket.close();
                } catch (IOException ignored) {
                }
            }
            videoSocket = null;
            return false;
        }
        return true;
    }

    public boolean requestAudioSocket() {
        var host        = controlSocket.getInetAddress().getHostAddress();
        var audioSocket = new Socket();
        try {
            audioSocket.connect(new InetSocketAddress(InetAddress.getByName(host), Session.AUDIO_PORT));
            var writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(audioSocket.getOutputStream())));
            writer.println(sessionID);
            writer.flush();
            this.audioSocket = audioSocket;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            if (audioSocket.isConnected()) {
                try {
                    audioSocket.close();
                } catch (IOException ignored) {
                }
            }
            audioSocket = null;
            return false;
        }
        return true;
    }

    byte[] tmp = new byte[20 * 1024 * 1024];

    public boolean receiveVideo(byte[] buffer) {
        if (videoSocket == null) return false;
        if (videoSocket.isClosed()) return false;
        try {
            var inputStream  = new ObjectInputStream(videoSocket.getInputStream());
            int length = inputStream.readInt();
            inputStream.readNBytes(tmp, 0, length);
            int uncompressedSize = Snappy.uncompress(tmp, 0, length, buffer, 0);
//            if (new BufferedInputStream(videoSocket.getInputStream()).readNBytes(buffer, 0, buffer.length) == -1)
//            var dataInputStream = new DataInputStream(videoSocket.getInputStream());
//            int len             = dataInputStream.readInt();
//            dataInputStream.read(tmp, 0, len);
//            Snappy.uncompress(tmp, 0, len, buffer, 0);

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean receiveAudio(byte[] buffer) {
        if (audioSocket == null) return false;
        if (audioSocket.isClosed()) return false;
        try {
            if (audioSocket.getInputStream().read(buffer) == -1)
                return false;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean sendCommand() {
        return false;
    }

    public static class Factory {
        public static ClientSession createClientSession(String address) {
            ClientSession clientSession = null;
            try {
                Socket controlSocket = new Socket(address, CONTROL_PORT);
                var    reader        = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                clientSession = new ClientSession(reader.readLine(), controlSocket);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return null;
            }
            return clientSession;
        }
    }

}
