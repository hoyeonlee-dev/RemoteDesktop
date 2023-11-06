package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.screenCapture.DXGIScreenCapture;
import kr.ac.hansung.remoteDesktop.screenCapture.GDIScreenCapture;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

public class Host {
    static long captureTime = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("서버");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

//        var panel = new JPanel(new GridLayout(2, 1));
//        frame.add(panel);
        var gdiCapture = new GDIScreenCapture(1920, 1080);
        var dxgiCapture = new DXGIScreenCapture(1920, 1080);
//        var sc = new ScreenCapture(5120, 1440);
//        sc.setVisible(true);
//        sc.initializeNative();


        var bitmapCanvas = new RemoteScreen(1920, 1080);
        frame.add(bitmapCanvas, BorderLayout.CENTER);
        var rect = new Rectangle(0, 0, 5120, 1440);
//        try {
//
//            Robot robot = new Robot();
//            var file = new File("sample.png");
////            var capture = robot.createScreenCapture(rect);
//            // var capture = sc.getCapturedImage();
//            var capture = dxgiCapture.createBufferedImage();
//            var result = ImageIO.write(capture, "png", file);
//            System.out.println(file.getAbsolutePath());
//            System.out.println(result);
//            System.exit(0);
//        } catch (IOException | AWTException e) {
//            System.err.println(e.getMessage());
//        }

        int count = 0;
        try (ServerSocket socket = new ServerSocket(1234)) {
            var accepted = socket.accept();
            System.out.println("Server: 클라이언트 접속을 확인했습니다.");
            frame.setName("서버 : 연결 확인");
            Robot robot = new Robot();
            ObjectOutputStream oos = new ObjectOutputStream((accepted.getOutputStream()));
            var os = accepted.getOutputStream();
            while (true) {
                var st = System.nanoTime();
//                  var capture = robot.createScreenCapture(rect);
//                  var capture = sc.getCapturedImage();
                var capture = dxgiCapture.createBufferedImage();
                os.write(dxgiCapture.getFrameBuffer());
//                var capture = gdiCapture.createBufferedImage();
//                os.write(gdiCapture.getBOS());
                os.flush();
//                        captureTime = System.nanoTime();
                bitmapCanvas.setImage(capture);
                bitmapCanvas.repaint();
//                    System.out.printf("%f ms\n last_drawn %d \n", ((double) System.nanoTime() - st) / 1_000_000, bitmapCanvas.lastDrawnTime);
            }
        } catch (IOException | AWTException e) {
            System.err.println(e.getMessage());
        }


    }
}
