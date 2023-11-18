package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.connection.client.ClientSession;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;
import kr.ac.hansung.remoteDesktop.window.event.StopStreamingOnClose;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RemoteClientWindow implements IRDPWindow, Runnable {
    private final JFrame clientWindow;
    private final RemoteScreen remoteScreen;
    byte[] buffer = null;
    private boolean shouldStop;

    public RemoteClientWindow(String title) {
        clientWindow = new JFrame(title);
        clientWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        clientWindow.setLayout(new BorderLayout());
        clientWindow.setSize(1920, 1080);
        clientWindow.setLocationRelativeTo(null);
        remoteScreen = new RemoteScreen(1920, 1080);
        clientWindow.add(remoteScreen, BorderLayout.CENTER);
        clientWindow.addWindowListener(new StopStreamingOnClose(this));

        clientWindow.setVisible(true);
        clientWindow.addWindowStateListener(e -> {
            if (e.getNewState() == WindowEvent.WINDOW_CLOSING)
                shouldStop = true;
        });
        shouldStop = false;

        buffer = new byte[1920 * 1080 * 4];
    }

    public static void main(String[] args) {
        new Thread(new RemoteClientWindow("클라이언트")).start();
    }

    public void showClient() {
        clientWindow.setVisible(true);
    }

    @Override
    public void stopWindowAndService() {
        shouldStop = true;
    }

    @Override
    public void add(Component component) {

    }

    public void hideClient() {
        clientWindow.setVisible(false);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void update() {
        remoteScreen.setImage(buffer);
        remoteScreen.repaint();
    }

    public void add(Component component, Object constraints) {
        clientWindow.add(component, constraints);
    }

    @Override
    public void run() {
        ClientSession clientSession = null;
        while (clientSession == null) {
            clientSession = ClientSession.Factory.createClientSession("192.168.1.117");
        }
        System.out.println("연결했습니다.");
        while (!clientSession.requestVideoSocket()) {
            //영상용 소켓을 요청합니다.
        }
        while (!clientSession.requestAudioSocket()) {
            // 오디오용 소켓을 요청합니다.
        }

        showClient();
        while (!shouldStop) {
            int len = clientSession.receiveVideo(getBuffer());
            if(len == -1) continue;
//            try {
////                remoteScreen.setImage(ImageIO.read(new ByteArrayInputStream(getBuffer(), 0, len)));
//                remoteScreen.setImage(Toolkit.getDefaultToolkit().createImage(getBuffer()));
//                remoteScreen.repaint();
//            } catch (IOException e) {
//            }
            remoteScreen.setImage(Toolkit.getDefaultToolkit().createImage(getBuffer()));
            remoteScreen.repaint();
        }
    }
}
