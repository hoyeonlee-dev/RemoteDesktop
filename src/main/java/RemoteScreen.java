import javax.swing.*;
import java.awt.*;

public class RemoteScreen extends JPanel {
    private Image image;
    long lastDrawnTime = 0;

    int width;
    int height;

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
        repaint();
    }

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

    static final int DEBUG_STRING_YPOS = 600;
    static final int DEBUG_STRING_XPOS = 100;
    static final int DEFAULT_FONT_SIZE = 20;
    Font font = new Font("Arial", Font.BOLD, DEFAULT_FONT_SIZE);


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
