package kr.ac.hansung.remoteDesktop.ui.component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;

public class RemoteScreen extends JPanel {
    static final int DEBUG_STRING_YPOS = 600;
    static final int DEBUG_STRING_XPOS = 100;
    static final int DEFAULT_FONT_SIZE = 20;
    long lastDrawnTime = 0;
    int width;
    int height;
    Font font = new Font("Arial", Font.BOLD, DEFAULT_FONT_SIZE);
    BufferedImage cursor;
    int mouseX = 0;
    int mouseY = 0;
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

    public void setImage(Image image) {
        this.image = image;
    }

    public void initCursor() {
        try {
            InputStream is = RemoteScreen.class.getClassLoader().getResourceAsStream("cursor_1.png");
            if (is == null) {
                throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
            }

            cursor = ImageIO.read(is);

            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    private void drawMousePointer(Graphics g, int x, int y) {
        if (cursor == null) {
            initCursor();
        }
        g.drawImage(cursor, x, y, null);
    }

    public void setImage(byte[] bytes) {
        if (this.image == null) {
            image = new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR);
        }
        var raster = ((BufferedImage) image).getRaster();
        var dataBuffer = (DataBufferByte) raster.getDataBuffer();
        System.arraycopy(bytes, 0, dataBuffer.getData(), 0, bytes.length);
    }

    public void setImage(byte[] bytes, int offset, int len) {
        if (this.image == null) {
            image = new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR);
        }
        var raster = ((BufferedImage) image).getRaster();
        var dataBuffer = (DataBufferByte) raster.getDataBuffer();
        System.arraycopy(bytes, offset, dataBuffer.getData(), 0, len);
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        if (image == null) {
            return;
        }
        g.drawImage(image, 0, 0, null);
        long currentTime = System.nanoTime();
        double timeElapsed = (double) (currentTime - lastDrawnTime) / 1_000_000;
        drawMousePointer(g, mouseX, mouseY);
        g.drawString(String.format("%f ms", timeElapsed), 300, 400);
    }

    @Override
    public void paint(Graphics g) {
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
        g.drawString(String.format("Draw Bitmap:%f ms\n", (double) (drawEnd - drawBegin) / 1_000_000),
                     DEBUG_STRING_XPOS, DEBUG_STRING_XPOS + DEFAULT_FONT_SIZE);
        g.drawString(String.format("Expected :  %d FPS\n", (int) (1000 / timeElapsed)), DEBUG_STRING_XPOS,
                     DEBUG_STRING_XPOS + DEFAULT_FONT_SIZE * 2);
        lastDrawnTime = currentTime;
        drawMousePointer(g, mouseX, mouseY);
        g.drawString(Long.toString(System.nanoTime()), 10, 10);
    }
}
