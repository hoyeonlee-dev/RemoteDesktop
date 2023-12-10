package kr.ac.hansung.remoteDesktop.ui.window.event;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

/**
 * 클라이언트 창이 닫혔을 때 서버쪽으로 종료메시지를 보내기 위해 작성한 클래스
 */
public class ClientWindowListener implements WindowListener {
    List<Runnable> windowClosedRunnable;

    @Override
    public void windowOpened(WindowEvent e) {
        // Do nothing
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (windowClosedRunnable == null) return;
        for (var runnable : windowClosedRunnable) {
            runnable.run();
        }
        e.getWindow().dispose();
    }

    public void setWindowClosedRunnable(List<Runnable> windowClosedRunnable) {
        this.windowClosedRunnable = windowClosedRunnable;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        if (windowClosedRunnable == null) return;
        for (var runnable : windowClosedRunnable) {
            runnable.run();
        }
        e.getWindow().dispose();
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // Do nothing
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // Do nothing
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // Do nothing
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // Do nothing
    }
}
