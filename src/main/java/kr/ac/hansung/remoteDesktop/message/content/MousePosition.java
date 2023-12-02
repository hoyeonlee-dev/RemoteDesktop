package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

public class MousePosition implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int     x;
    private final int     y;
    private final boolean isClick;

    public MousePosition(int x, int y, boolean isClick) {
        this.x       = x;
        this.y       = y;
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
