package kr.ac.hansung.remoteDesktop.screenCapture;

import kr.ac.hansung.remoteDesktop.util.DLLLoader;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

/**
 * GDI 네이티브 API를 사용하여 화면을 캡처하는 클래스
 * java.awt.Robot보다 적은 메모리를 사용하고 초당 프레임이 높지만
 * {@link kr.ac.hansung.remoteDesktop.screenCapture.DXGIScreenCapture} 보다 상대적으로 느림
 * 주의 : dll을 로딩하지 못하면 Error발생
 *
 * @author hoyeon
 */
public class GDIScreenCapture implements IScreenCapture, ICaptureResult {
    private final static String LIBRARY_NAME = "GDIScreenCapture.dll";
    static {
        DLLLoader.LoadDLL(LIBRARY_NAME);
    }

    BufferedImage bufferedImage;
    private int height;
    private int width;
    private final byte[] frameBuffer;

    public GDIScreenCapture(int width, int height) {
        this.height = height;
        this.width = width;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        frameBuffer = new byte[width * height * 4];
        onWindowSizeUpdated();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        onWindowSizeUpdated();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        onWindowSizeUpdated();
    }

    @Override
    public byte[] getFrameBuffer() {
        return frameBuffer;
    }

    public void onWindowSizeUpdated() {

    }

    private native void updateWindowSize();

    private native byte[] getCapturedScreenByteArray();

    private native String getLogMessages();

    private native String getErrorMessages();

    public BufferedImage getBufferedImage() {
        if (frameBuffer.length <= 1024) {
//            System.err.println("RawBits is too small : " + rawBits.length);
//            System.err.println(getLogMessages());
//            System.err.println(getErrorMessages());
        }

        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        System.arraycopy(frameBuffer, 0, dataBuffer.getData(), 0, Integer.min(frameBuffer.length, dataBuffer.getData().length));

        return bufferedImage;
    }

    @Override
    public void doCapture() {
        getCapturedScreenByteArray();
    }
}
