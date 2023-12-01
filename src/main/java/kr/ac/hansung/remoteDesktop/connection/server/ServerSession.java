package kr.ac.hansung.remoteDesktop.connection.server;


import kr.ac.hansung.remoteDesktop.connection.Session;
import kr.ac.hansung.remoteDesktop.network.message.FileSendRequest;
import kr.ac.hansung.remoteDesktop.network.message.ImageInfo;
import kr.ac.hansung.remoteDesktop.network.message.MousePosition;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSession extends Session {

    byte[] compressBuffer;

    public ServerSession(Socket videoSocket, Socket controlSocket) {
        super(videoSocket, controlSocket);
        compressBuffer = new byte[20 * 1024 * 1024];
    }

    BufferedImage         bufferedImage             = null;
    ObjectOutputStream    videoInfoOutputStream     = null;
    ObjectOutputStream    controlObjectOutputStream = null;

    public void sendNoUpdate() {
        if (videoSocket == null) return;
        if (videoSocket.isClosed()) return;
        try {
            if (videoInfoOutputStream == null) videoInfoOutputStream = new ObjectOutputStream(videoSocket.getOutputStream());
            videoInfoOutputStream.writeObject(new ImageInfo(ImageInfo.Type.NO_UPDATE, -1));
            videoInfoOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMousePosition(int x, int y) {
        if (controlSocket == null) return;
        if (controlSocket.isClosed()) return;
        try {
            if (controlObjectOutputStream == null) controlObjectOutputStream = new ObjectOutputStream(controlSocket.getOutputStream());
            controlObjectOutputStream.writeObject(new MousePosition(x, y));
            controlObjectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendVideo(byte[] buffer, int width, int height) {
        if (videoSocket == null) return false;
        if (videoSocket.isClosed()) return false;
        if (bufferedImage == null) bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        else if (bufferedImage.getWidth() != width
                || bufferedImage.getHeight() != height) bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        if (buffer.length == 0) {
            try {
                videoInfoOutputStream.writeInt(0);
                return true;
            } catch (IOException e) {
            }
        }
        var imageBuffer = (DataBufferByte) (bufferedImage.getRaster().getDataBuffer());
        System.arraycopy(buffer, 0, imageBuffer.getData(), 0, buffer.length);
        try (var compressor = new TJCompressor(buffer, 0, 0, 1920, 3 * 1920, 1080, TJ.PF_BGR);) {

            compressor.set(TJ.PARAM_SUBSAMP, TJ.SAMP_420);
            compressor.set(TJ.PARAM_QUALITY, 80);
            compressor.set(TJ.PARAM_FASTDCT, 1);
            compressor.compress(compressBuffer);

            if (videoInfoOutputStream == null) videoInfoOutputStream = new ObjectOutputStream(videoSocket.getOutputStream());

            videoInfoOutputStream.writeObject(new ImageInfo(ImageInfo.Type.UPDATE, compressor.getCompressedSize()));
            videoInfoOutputStream.write(compressBuffer, 0, compressor.getCompressedSize());
            videoInfoOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    ObjectInputStream controlInputStream;

    public void checkFileTransferRequest() {
        try {
            if (controlInputStream == null) controlInputStream = new ObjectInputStream(controlSocket.getInputStream());
            var message = controlInputStream.readObject();
            if (message instanceof FileSendRequest) {
                var fileSendRequest = (FileSendRequest) message;
                for (var fileName : fileSendRequest.fileNames()) {
                    System.out.println(fileName);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
