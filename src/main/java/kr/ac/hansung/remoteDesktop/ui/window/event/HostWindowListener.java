package kr.ac.hansung.remoteDesktop.ui.window.event;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

/**
 * 서버의 윈도우가 닫혔을 때 처리할 동작들을 일괄로 처리하기 위해 작성한 클래스
 */
public class HostWindowListener implements WindowListener {
    List<Runnable> windowCloseRunnable;

    @Override
    public void windowOpened(WindowEvent e) {
        // Do nothing
    }

    public void setWindowCloseRunnable(List<Runnable> windowCloseRunnable) {
        this.windowCloseRunnable = windowCloseRunnable;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (windowCloseRunnable == null) return;
        for (var runnable : windowCloseRunnable) {
            runnable.run();
        }
        e.getWindow().setVisible(false);
    }


    @Override
    public void windowClosed(WindowEvent e) {
        if (windowCloseRunnable == null) return;
        for (var runnable : windowCloseRunnable) {
            runnable.run();
        }
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
