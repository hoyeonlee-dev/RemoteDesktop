package kr.ac.hansung.remoteDesktop.window;

import kr.ac.hansung.remoteDesktop.connection.client.ClientSession;
import kr.ac.hansung.remoteDesktop.ui.RemoteScreen;
import kr.ac.hansung.remoteDesktop.window.event.FileDropListener;
import kr.ac.hansung.remoteDesktop.window.event.FileDropListenerActionImpl;
import kr.ac.hansung.remoteDesktop.window.event.StopStreamingOnClose;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RemoteClientWindow implements IRDPWindow, Runnable {
    private JFrame clientWindow;
    byte[] buffer = null;
    private final RemoteScreen remoteScreen;

    private final String address;

    FileDropListener.FileDropAction fileDropAction;
    private boolean shouldStop;

    public RemoteClientWindow(String title, String address) {
        remoteScreen = createWindow(title);
        this.address = address;
        shouldStop   = false;

        buffer = new byte[1920 * 1080 * 4];
    }

    public void setTitle(String title) {
        if (clientWindow != null) {
            clientWindow.setTitle(title);
        }
    }

    private RemoteScreen createWindow(String title) {
        final RemoteScreen remoteScreen;
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
        return remoteScreen;
    }

    public void showClient() {
        clientWindow.setVisible(true);
    }

    public void hideClient() {
        clientWindow.setVisible(false);
    }

    @Override
    public void stopWindowAndService() {
        shouldStop = true;
    }

    @Override
    public void add(Component component) {

    }

    public void add(Component component, Object constraints) {
        clientWindow.add(component, constraints);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void updateRemoteScreen() throws IOException {
        remoteScreen.setImage(ImageIO.read(new ByteArrayInputStream(getBuffer())));
        remoteScreen.repaint();
    }

    ClientSession clientSession = null;

    private void init() {
        while (clientSession == null) {
            clientSession = ClientSession.Factory.createClientSession(address);
            var fileDropListener = new FileDropListener();
            fileDropAction = new FileDropListenerActionImpl(clientSession);

            fileDropListener.addFileDropAction(fileDropAction);
            new DropTarget(clientWindow, fileDropListener);
        }
        System.out.println("연결했습니다.");
    }

    @Override
    public void run() {
        init();
        new Thread(() -> {
            while (true) {
                var mousePosition = clientSession.receiveMousePosition();
                remoteScreen.setMouseX(mousePosition.x());
                remoteScreen.setMouseY(mousePosition.y());
            }
        }).start();

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
