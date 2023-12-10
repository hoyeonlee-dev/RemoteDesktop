package kr.ac.hansung.remoteDesktop.client.receiver;

import kr.ac.hansung.remoteDesktop.message.content.ImageInfo;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * 서버로부터 전송된 영상을 수신하는 클래스
 */
public class VideoReceiver implements Closeable {

    private final Socket videoSocket;
    byte[] tmp = new byte[20 * 1024 * 1024];
    private ObjectInputStream objectInputStream;
    private boolean isClosed;

    public VideoReceiver(Socket videoSocket) {
        this.videoSocket = videoSocket;
        isClosed = false;
    }

    public int receiveVideo(byte[] buffer) throws IOException {
        if (isClosed) return -1;
        try {
            if (objectInputStream == null) {
                var inputStream = videoSocket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);
            }
            var imageInfo = (ImageInfo) objectInputStream.readObject();
            System.out.printf("Client received: %s\n", imageInfo.toString());
            if (imageInfo.getType() == ImageInfo.Type.NO_UPDATE) {
                return 0;
            }
            int length = imageInfo.getSize();
            if (length == 0) return tmp.length;
            objectInputStream.readNBytes(tmp, 0, length);
            System.arraycopy(tmp, 0, buffer, 0, length);
            return length;
        } catch (IOException e) {
            if (e instanceof EOFException) return -1;
//            e.printStackTrace();
            System.err.println(e.getMessage());
            return -100;
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
            return -100;
        }
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        if (objectInputStream != null)
            objectInputStream.close();
    }
}
