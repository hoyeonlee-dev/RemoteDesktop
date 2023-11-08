package kr.ac.hansung.remoteDesktop.window;


import java.awt.*;

public interface IRDPWindow {
    void showClient();

    void makeItStop();

    void hideClient();

    void add(Component component, Object constraints);
}
