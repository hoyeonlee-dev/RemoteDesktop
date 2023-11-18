package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.screenCapture.DXGIScreenCapture;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;
import org.xerial.snappy.Snappy;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class EncodingTest {

    public static void main(String[] args) {

        var t1 = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(10240)) {
                while (true) {
                    var accepted = serverSocket.accept();
                    new Thread(new VideoThread(accepted)).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        var t2 = new Thread(() -> {
            while (true) {
                byte[] buffer      = new byte[1024 * 1024 * 20];
                byte[] videoBuffer = new byte[1024 * 1024 * 20];
                var    jframe      = new JFrame("title");
                jframe.setSize(1280, 720);
                var screen = new RemoteScreen(1920, 1080);
                jframe.add(screen);
                jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                jframe.setVisible(true);

                try (Socket socket = new Socket("192.168.1.117", 10240)) {
                    var outputStream = socket.getOutputStream();
                    var inputStream  = new ObjectInputStream(socket.getInputStream());
                    while (true) {
                        int length = inputStream.readInt();
                        inputStream.readNBytes(buffer, 0, length);
//                        if (Snappy.isValidCompressedBuffer(buffer, 0, length)) {
                        int uncompressedSize = Snappy.uncompress(buffer, 0, length, videoBuffer, 0);
//                        System.out.println(length / 1024);
                        screen.setImage(videoBuffer, 0, uncompressedSize);
                        screen.repaint();
//                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t1.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t2.start();
    }

    static class VideoThread implements Runnable {
        static final int    EXPECTED_FPS = 60;
        final        Socket accepted;
        byte[] buffer;

        public VideoThread(Socket socket) {
            this.accepted = socket;
            buffer = new byte[20 * 1024 * 1024];
        }

        @Override
        public void run() {
            try {
                var             dxgiScreenCapture = new DXGIScreenCapture(1920, 1080);
                var             outputStream      = new ObjectOutputStream(accepted.getOutputStream());
                long            lastSentTime      = System.nanoTime();
                while (true) {
//                    if (((System.nanoTime() - lastSentTime) / 1_000_000) < (1000 / EXPECTED_FPS)) continue;
                    //System.out.printf("%d, %d\n", (System.nanoTime() - lastSentTime) / 1_000_000, (1000 / EXPECTED_FPS));
                    dxgiScreenCapture.doCapture();
                    int length = Snappy.compress(dxgiScreenCapture.getFrameBuffer(), 0, dxgiScreenCapture.getFrameBuffer().length, buffer, 0);
                    outputStream.writeInt(length);
                    outputStream.write(buffer, 0, length);
//                    System.out.println(length / 1024);
//                    outputStream.writeUnshared(bufferedMessage);
                    outputStream.flush();
                    lastSentTime = System.nanoTime();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
