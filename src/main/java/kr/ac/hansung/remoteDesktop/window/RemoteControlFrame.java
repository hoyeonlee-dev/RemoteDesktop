package kr.ac.hansung.remoteDesktop.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;

public class RemoteControlFrame extends JPanel {
    private RemoteMouseSender remoteMouseSender;

    public RemoteControlFrame(RemoteMouseSender remoteMouseSender) {
        this.remoteMouseSender = remoteMouseSender;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    // 마우스 클릭 이벤트 전송
                    remoteMouseSender.sendMouseClick(e.getButton(), true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                try {
                    // 마우스 이동 이벤트 전송
                    remoteMouseSender.sendMouseMove(e.getX(), e.getY());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
