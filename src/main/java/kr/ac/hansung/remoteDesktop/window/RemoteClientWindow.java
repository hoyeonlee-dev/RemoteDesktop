package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.connection.client.ClientSession;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;
import kr.ac.hansung.remoteDesktop.window.event.FileDropListener;
import kr.ac.hansung.remoteDesktop.window.event.StopStreamingOnClose;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteClientWindow implements IRDPWindow, Runnable {
    private final JFrame       clientWindow;
    private final RemoteScreen remoteScreen;
    byte[] buffer = null;
    private boolean shouldStop;
    FileDropListener.FileDropAction defaultFileDropAction = new FileDropListener.FileDropAction() {
        @Override
        public void fileDropped(File file) {
            var files = new ArrayList<File>();
            files.add(file);
            new Thread(() -> {
                try {
                    clientSession.sendFileTransferRequest(files);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        @Override
        public void fileDropped(List<File> files) {
            new Thread(() -> {
                try {
                    clientSession.sendFileTransferRequest(files);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    };

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
        var fileDropListener = new FileDropListener();
        fileDropListener.addFileDropAction(defaultFileDropAction);
        new DropTarget(clientWindow, fileDropListener);
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

    public void updateRemoteScreen() throws IOException {
        remoteScreen.setImage(ImageIO.read(new ByteArrayInputStream(getBuffer())));
        remoteScreen.repaint();
    }

    public void add(Component component, Object constraints) {
        clientWindow.add(component, constraints);
    }

    ClientSession clientSession = null;

    private void init() {
        while (clientSession == null) {
            clientSession = ClientSession.Factory.createClientSession("192.168.1.117");
        }
        System.out.println("연결했습니다.");
    }

    @Override
    public void run() {
        init();

        showClient();

        while (!shouldStop) {
            int len = clientSession.receiveVideo(getBuffer());
            if (len == -1) continue;
            try {
                updateRemoteScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
