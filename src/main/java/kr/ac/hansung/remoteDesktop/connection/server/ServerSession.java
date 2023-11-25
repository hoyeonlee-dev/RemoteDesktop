package kr.ac.hansung.remoteDesktop.connection.server;


import kr.ac.hansung.remoteDesktop.connection.Session;
import kr.ac.hansung.remoteDesktop.network.message.FileSendRequest;
import kr.ac.hansung.remoteDesktop.network.message.ImageInfo;
import kr.ac.hansung.remoteDesktop.network.message.ImageType;
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

    public ServerSession(Socket videoSocket, Socket audioSocket, Socket controlSocket) {
        super(videoSocket, audioSocket, controlSocket);
        compressBuffer = new byte[20 * 1024 * 1024];
    }

    BufferedImage         bufferedImage         = null;
    ImageOutputStream     imageOutputStream     = null;
    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream    objectOutputStream    = null;

    public void sendNoUpdate() {
        if (videoSocket == null) return;
        if (videoSocket.isClosed()) return;
        try {
            if (objectOutputStream == null) objectOutputStream = new ObjectOutputStream(videoSocket.getOutputStream());
            objectOutputStream.writeObject(new ImageInfo(ImageType.NO_UPDATE, -1));
            objectOutputStream.flush();
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
                objectOutputStream.writeInt(0);
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

            if (objectOutputStream == null) objectOutputStream = new ObjectOutputStream(videoSocket.getOutputStream());

            objectOutputStream.writeObject(new ImageInfo(ImageType.UPDATE, compressor.getCompressedSize()));
            objectOutputStream.write(compressBuffer, 0, compressor.getCompressedSize());
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendAudio(byte[] buffer) {
        if (audioSocket == null) return false;
        if (audioSocket.isClosed()) return false;
        try (var objectOutputStream = new ObjectOutputStream(audioSocket.getOutputStream())) {
            objectOutputStream.writeInt(buffer.length);
            objectOutputStream.write(buffer, 0, buffer.length);
            objectOutputStream.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
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
