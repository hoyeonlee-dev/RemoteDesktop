package kr.ac.hansung.remoteDesktop.window.event;

import kr.ac.hansung.remoteDesktop.window.IRDPWindow;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StopStreamingOnClose extends WindowAdapter {
    private IRDPWindow window;

    public StopStreamingOnClose(IRDPWindow window) {
        this.window = window;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        window.makeItStop();
    }


}
