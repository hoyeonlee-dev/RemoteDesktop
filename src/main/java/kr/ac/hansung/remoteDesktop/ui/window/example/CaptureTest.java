package kr.ac.hansung.remoteDesktop.ui.window.example;

import kr.ac.hansung.remoteDesktop.ui.component.RemoteScreen;
import kr.ac.hansung.remoteDesktop.util.screenCapture.DXGIScreenCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CaptureTest {
    static long captureTime = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("캡처 테스트");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        var dxgiCapture = new DXGIScreenCapture(1920, 1080);
        dxgiCapture.setFrameRate(100);

        var bitmapCanvas = new RemoteScreen(1920, 1080);
        frame.add(bitmapCanvas, BorderLayout.CENTER);
        var rect = new Rectangle(0, 0, 1920, 1080);

//        takeScreenShot(dxgiCapture.getBufferedImage());

        int count = 0;
        while (true) {
            var st = System.nanoTime();
//            var capture = robot.createScreenCapture(rect);
//            var capture = sc.getCapturedImage();
            var capture = dxgiCapture.getBufferedImage();
//            var capture = gdiCapture.createBufferedImage();
//            os.write(gdiCapture.getBOS());
//            captureTime = System.nanoTime();
            bitmapCanvas.setImage(capture);
            bitmapCanvas.repaint();
//            System.out.printf("%f ms\n last_drawn %d \n", ((double) System.nanoTime() - st) / 1_000_000, bitmapCanvas.lastDrawnTime);
        }
    }

    private static void takeScreenShot(BufferedImage image) {
        File file = new File("sample.png");
        try {
            ImageIO.write(image, "png", file);

            System.out.printf("%s에 저장했습니다.", file.getAbsolutePath());
        } catch (IOException ignored) {

        }
    }
}
