package kr.ac.hansung.screenCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

public class GDIScreenCapture implements IScreenCapture, IBufferedCapture{
    private int height;
    private int width;

    private byte[] frameBuffer;

    static {
//        System.load("C:\\Users\\hoyeon\\RiderProjects\\GDIScreenCapture\\x64\\Debug\\GDIScreenCapture.dll");
        System.load("C:\\Users\\hoyeon\\source\\Network_Programming\\GDIScreenCapture\\x64\\Release\\GDIScreenCapture.dll");
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

    public GDIScreenCapture(int width, int height) {
        this.height = height;
        this.width = width;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        frameBuffer = new byte[width * height * 4];
        onWindowSizeUpdated();
    }

    public void onWindowSizeUpdated() {

    }

    @Override
    public byte[] getFrameBuffer() {
        return frameBuffer;
    }

    private native void updateWindowSize();

    private native byte[] getCapturedScreenByteArray();

    private native String getLogMessages();

    private native String getErrorMessages();

    BufferedImage bufferedImage;

    public BufferedImage createBufferedImage() {
        var rawBits = getCapturedScreenByteArray();

        if (rawBits.length <= 1024) {
//            System.err.println("RawBits is too small : " + rawBits.length);
//            System.err.println(getLogMessages());
//            System.err.println(getErrorMessages());
        }

        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        System.arraycopy(rawBits, 0, dataBuffer.getData(), 0, Integer.min(rawBits.length, frameBuffer.length));

        return bufferedImage;
    }
}
