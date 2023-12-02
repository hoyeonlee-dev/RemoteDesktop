package kr.ac.hansung.remoteDesktop.ui.window;


import java.awt.*;

public interface IRDPWindow {
    void showClient();

    void hideClient();

    void stopWindowAndService();

    void add(Component component);

    void add(Component component, Object constraints);
}
