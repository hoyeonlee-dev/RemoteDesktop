package kr.ac.hansung.remoteDesktop.util.screenCapture;

import kr.ac.hansung.remoteDesktop.util.DLLLoader;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

/**
 * DXGI 네이티브 API를 사용하여 화면을 캡처하는 클래스
 * java.awt.Robot보다 빠름
 * 주의 : dll을 로딩하지 못하면 Error발생
 *
 * @author hoyeon
 */
public class DXGIScreenCapture implements IScreenCapture, ICaptureResult {
    private static final String LIBRARY_NAME = "DXGIScreenCapture.dll";

    static {
        DLLLoader.LoadDLL(LIBRARY_NAME);
    }

    private final byte[] frameBuffer;
    BufferedImage bufferedImage;
    private int width;
    private int height;
    private int frameRate;

    public DXGIScreenCapture(int width, int height) {
        this.width = width;
        this.height = height;
        frameRate = 60;
        // bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        frameBuffer = new byte[width * height * 3];
        onWindowSizeUpdated();

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

    @Override
    public byte[] getFrameBuffer() {
        return frameBuffer;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        onWindowSizeUpdated();
    }

    //DLL을 이용한 네이티브 메서드 호출
    private native void updateWindowSize();

    private native boolean getCapturedScreenByteArray();

    private native String getLogMessages();

    private native String getErrorMessages();

    @Override
    public void onWindowSizeUpdated() {
        updateWindowSize();
        System.out.println(getLogMessages());
    }

    @Override
    public BufferedImage getBufferedImage() {
        if (frameBuffer == null) {
            return bufferedImage;
        }
        if (frameBuffer.length <= 1024) {
            System.err.println("RawBits is too small : " + frameBuffer.length);
        }

        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        System.arraycopy(frameBuffer, 0, dataBuffer.getData(), 0,
                         Integer.min(frameBuffer.length, dataBuffer.getData().length));

        return bufferedImage;
    }

    // 서버가 화면을 캡처할 때 사용하는 메서드
    @Override
    public boolean doCapture() {
        return getCapturedScreenByteArray();
    }
}
