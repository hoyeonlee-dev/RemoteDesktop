package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        JFrame frame = new JFrame("클라이언트");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        var bitmapCanvas = new RemoteScreen(1920, 1080);
        frame.add(bitmapCanvas, BorderLayout.CENTER);
        var rect = new Rectangle(0, 0, 1920, 1080);

        byte[] bytes = new byte[1920 * 1080 * 4];

        try (Socket socket = new Socket("localhost", 1234);) {
            System.out.println("클라이언트 : 접속했습니다.");
            frame.setName("클라이언트 : 연결 확인");
            try {
                var ios = new ObjectInputStream((socket.getInputStream()));
                var is = socket.getInputStream();
                BufferedImage bufferedImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_4BYTE_ABGR);
                while (true) {

//                        var msg = (BufferedWrapper) ios.readObject();
//                        var msg = (Message) ios.readObject();
//                        Message.copyMessage(msg, bytes);
                    is.readNBytes(bytes, 0, bytes.length);

                    var raster = bufferedImage.getRaster();
                    var buf = (DataBufferByte) (raster.getDataBuffer());
                    System.arraycopy(bytes, 0, buf.getData(), 0, bytes.length);

//                        bitmapCanvas.setImage(msg.getBufferedImage());
                    bitmapCanvas.setImage(bufferedImage);
                    bitmapCanvas.repaint();
                }
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
