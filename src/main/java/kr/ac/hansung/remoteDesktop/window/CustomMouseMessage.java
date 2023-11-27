package kr.ac.hansung.remoteDesktop.window;

import java.io.Serializable;

public class CustomMouseMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private int x;
    private int y;
    private boolean isClick;

    public CustomMouseMessage(int x, int y, boolean isClick) {
        this.x = x;
        this.y = y;
        this.isClick = isClick;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isClick() {
        return isClick;
    }
}
