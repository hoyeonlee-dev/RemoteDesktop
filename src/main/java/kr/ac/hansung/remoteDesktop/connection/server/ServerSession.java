package kr.ac.hansung.remoteDesktop.connection.server;


import kr.ac.hansung.remoteDesktop.BGRtoJPGConverter;
import kr.ac.hansung.remoteDesktop.VideoOutputStream;
import kr.ac.hansung.remoteDesktop.connection.Session;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;
import org.xerial.snappy.Snappy;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSession extends Session {

    byte[] compressBuffer;

    public ServerSession(Socket videoSocket, Socket audioSocket, Socket controlSocket) {
        super(videoSocket, audioSocket, controlSocket);
        compressBuffer = new byte[20 * 1024 * 1024];
    }

    BufferedImage         bufferedImage         = null;
    VideoOutputStream     videoOutputStream     = null;
    ImageOutputStream     imageOutputStream     = null;
    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream    objectOutputStream    = null;

    public boolean sendVideo(byte[] buffer, int width, int height) {
        if (videoSocket == null) return false;
        if (videoSocket.isClosed()) return false;
        if (bufferedImage == null) bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        else if (bufferedImage.getWidth() != width
                || bufferedImage.getHeight() != height) bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        var imageBuffer = (DataBufferByte) (bufferedImage.getRaster().getDataBuffer());
        System.arraycopy(buffer, 0, imageBuffer.getData(), 0, buffer.length);
        try (var compressor = new TJCompressor(buffer, 0, 0, 1920, 3 * 1920, 1080, TJ.PF_BGR);) {
//            if (objectOutputStream == null) objectOutputStream = new ObjectOutputStream(videoSocket.getOutputStream());
//            var jpg = BGRtoJPGConverter.convertBGRToJPG(buffer, width, height);
//            objectOutputStream.writeInt(jpg.length);
//            objectOutputStream.write(jpg);
//            if (videoOutputStream == null) {
//                videoOutputStream = new VideoOutputStream(new ObjectOutputStream(videoSocket.getOutputStream()));
//            }
//            var imageOutputStream = ImageIO.createImageOutputStream(videoOutputStream);
//            ImageIO.setUseCache(false);
//            ImageIO.write(bufferedImage, "jpg", imageOutputStream);
//            imageOutputStream.flush();

            compressor.set(TJ.PARAM_SUBSAMP, TJ.SAMP_420);
            compressor.set(TJ.PARAM_QUALITY, 80);
            compressor.set(TJ.PARAM_FASTDCT, 1);
            compressor.compress(compressBuffer);

            if (objectOutputStream == null) objectOutputStream = new ObjectOutputStream(videoSocket.getOutputStream());
//            var jpg = BGRtoJPGConverter.convertBGRToJPG(buffer, width, height);
            objectOutputStream.writeInt(compressor.getCompressedSize());
            objectOutputStream.write(compressBuffer, 0, compressor.getCompressedSize());

//            imageOutputStream.close();
//            videoOutputStream.transferAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
//            System.err.println(e.getMessage());
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
