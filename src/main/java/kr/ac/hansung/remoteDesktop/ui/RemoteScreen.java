package kr.ac.hansung.remoteDesktop.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class RemoteScreen extends JPanel {
    static final int DEBUG_STRING_YPOS = 600;
    static final int DEBUG_STRING_XPOS = 100;
    static final int DEFAULT_FONT_SIZE = 20;
    long lastDrawnTime = 0;
    int width;
    int height;
    Font font = new Font("Arial", Font.BOLD, DEFAULT_FONT_SIZE);
    private Image image;

    public RemoteScreen(int width, int height) {
        super();
        setDoubleBuffered(true);
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
    }

    public RemoteScreen() {
        super();
        setDoubleBuffered(true);
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public synchronized void setImage(Image image) {
        this.image = image;
    }

    public synchronized void setImage(byte[] bytes) {
        if (this.image == null) {
            image = new BufferedImage(1920, 1080, BufferedImage.TYPE_4BYTE_ABGR);
        }
        var raster = ((BufferedImage) image).getRaster();
        var dataBuffer = (DataBufferByte) raster.getDataBuffer();
        System.arraycopy(bytes, 0, dataBuffer.getData(), 0, bytes.length);
    }

    @Override
    public synchronized void paint(Graphics g) {
        //super.paint(g);
        g.setFont(font);
        if (image == null) {
            return;
        }
        long drawBegin = System.nanoTime();
        g.drawImage(image, 0, 0, null);
        long drawEnd = System.nanoTime();

        long currentTime = System.nanoTime();
        double timeElapsed = (double) (currentTime - lastDrawnTime) / 1_000_000;
        g.drawString(String.format("Elapsed  :  %f ms\n", timeElapsed), DEBUG_STRING_XPOS, DEBUG_STRING_XPOS);
        g.drawString(String.format("Draw Bitmap:%f ms\n", (double) (drawEnd - drawBegin) / 1_000_000), DEBUG_STRING_XPOS, DEBUG_STRING_XPOS + DEFAULT_FONT_SIZE);
        g.drawString(String.format("Expected :  %d FPS\n", (int) (1000 / timeElapsed)), DEBUG_STRING_XPOS, DEBUG_STRING_XPOS + DEFAULT_FONT_SIZE * 2);
        lastDrawnTime = currentTime;
        g.drawString(Long.toString(System.nanoTime()), 10, 10);
    }

    @Override
    public synchronized void update(Graphics g) {
        super.update(g);
        if (image == null) {
            return;
        }
        g.drawImage(image, 0, 0, null);
        long currentTime = System.nanoTime();
        double timeElapsed = (double) (currentTime - lastDrawnTime) / 1_000_000;
        g.drawString(String.format("%f ms", timeElapsed), 300, 400);
    }
}
