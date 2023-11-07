package kr.ac.hansung.remoteDesktop.screenCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

/**
 * DXGI 네이티브 API를 사용하여 화면을 캡처하는 클래스
 * java.awt.Robot과 {@link kr.ac.hansung.remoteDesktop.screenCapture.GDIScreenCapture}보다 빠름
 * 주의 : dll을 로딩하지 못하면 Error발생
 * @author hoyeon
 */
public class DXGIScreenCapture implements IScreenCapture, IBufferedCapture{
    private int width;
    private int height;

    private int frameRate;

    private byte[] frameBuffer;

    static {
        System.load("C:\\Users\\imyee\\Downloads\\DXGIScreenCapture.dll");
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        onWindowSizeUpdated();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        onWindowSizeUpdated();
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        onWindowSizeUpdated();
    }

    public DXGIScreenCapture(int width, int height) {
        this.width = width;
        this.height = height;
        frameRate = 60;
        // bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        frameBuffer = new byte[width * height * 4];
        onWindowSizeUpdated();
    }

    private native void updateWindowSize();

    private native byte[] getCapturedScreenByteArray();

    private native String getLogMessages();

    private native String getErrorMessages();

    @Override
    public void onWindowSizeUpdated() {
        updateWindowSize();
        System.out.println(getLogMessages());
    }

    BufferedImage bufferedImage;
    public int len;

    @Override
    public byte[] getFrameBuffer() {
        return frameBuffer;
    }

    @Override
    public BufferedImage createBufferedImage() {
        var rawBits = getCapturedScreenByteArray();

        if (rawBits == null) {
            return bufferedImage;
        }
        if (rawBits.length <= 1024) {
            System.err.println("RawBits is too small : " + rawBits.length);
//            System.err.println(getLogMessages());
//            System.err.println(getErrorMessages());
        }

        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        System.arraycopy(rawBits, 0, dataBuffer.getData(), 0, Integer.min(rawBits.length, frameBuffer.length));

        return bufferedImage;
    }
}
