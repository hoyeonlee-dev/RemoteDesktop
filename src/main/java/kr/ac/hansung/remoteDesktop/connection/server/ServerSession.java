package kr.ac.hansung.remoteDesktop.connection.server;


import kr.ac.hansung.remoteDesktop.connection.Session;
import org.xerial.snappy.Snappy;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSession extends Session {

    byte[] compressBuffer;
    public ServerSession(Socket videoSocket, Socket audioSocket, Socket controlSocket) {
        super(videoSocket, audioSocket, controlSocket);
        compressBuffer= new byte[20 * 1024 * 1024];
    }

    public boolean sendVideo(byte[] buffer) {
        if (videoSocket == null) return false;
        if (videoSocket.isClosed()) return false;

        try {
            var outputStream     = new ObjectOutputStream(videoSocket.getOutputStream());
//            var dataOutputStream = new DataOutputStream(videoSocket.getOutputStream());
//            dataOutputStream.writeInt(buffer.length);
//            dataOutputStream.write(buffer, 0, buffer.length);
//            dataOutputStream.flush();
            int length = Snappy.compress(buffer, 0, buffer.length, compressBuffer, 0);
            outputStream.writeInt(length);
            outputStream.write(compressBuffer, 0, length);
            outputStream.flush();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean sendAudio(byte[] buffer) {
        if (audioSocket == null) return false;
        if (audioSocket.isClosed()) return false;
        try {
            audioSocket.getOutputStream().write(buffer);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
}
