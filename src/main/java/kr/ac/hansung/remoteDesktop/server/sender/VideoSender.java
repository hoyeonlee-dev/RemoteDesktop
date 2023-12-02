package kr.ac.hansung.remoteDesktop.server.sender;

import kr.ac.hansung.remoteDesktop.message.content.ImageInfo;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class VideoSender implements Closeable {

    private final ObjectOutputStream videoInfoOutputStream;

    private final byte[]        compressBuffer;
    private       BufferedImage bufferedImage = null;
    private       boolean       isClosed;

    public VideoSender(ObjectOutputStream videoOutputStream) throws IOException {
        videoInfoOutputStream = videoOutputStream;
        compressBuffer        = new byte[20 * 1024 * 1024];
        isClosed              = false;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void sendNoUpdate() {
        if (isClosed) return;
        try {
            videoInfoOutputStream.writeObject(new ImageInfo(ImageInfo.Type.NO_UPDATE, 0));
            videoInfoOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendVideo(byte[] buffer, int width, int height) {
        if (isClosed) return false;
        if (bufferedImage == null) bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        else if (bufferedImage.getWidth() != width || bufferedImage.getHeight() != height) bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        if (buffer.length == 0) {
            try {
                videoInfoOutputStream.writeInt(0);
                return true;
            } catch (IOException e) {
            }
        }
        var imageBuffer = (DataBufferByte) (bufferedImage.getRaster().getDataBuffer());
        System.arraycopy(buffer, 0, imageBuffer.getData(), 0, buffer.length);
        try (var compressor = new TJCompressor(buffer, 0, 0, 1920, 3 * 1920, 1080, TJ.PF_BGR)) {

            compressor.set(TJ.PARAM_SUBSAMP, TJ.SAMP_420);
            compressor.set(TJ.PARAM_QUALITY, 80);
            compressor.set(TJ.PARAM_FASTDCT, 1);
            compressor.compress(compressBuffer);

            videoInfoOutputStream.writeObject(new ImageInfo(ImageInfo.Type.UPDATE, compressor.getCompressedSize()));
            videoInfoOutputStream.write(compressBuffer, 0, compressor.getCompressedSize());
            videoInfoOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }
}
