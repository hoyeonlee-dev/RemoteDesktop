package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 클라이언트가 서버의 마우스를 이동할 때 사용
// 서버의 마우스 위치를 클라이언트에 전송할 때 사용
public class MousePosition implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int x;
    private final int y;
    private final boolean isClick;

    public MousePosition(int x, int y, boolean isClick) {
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

    @Override
    public String toString() {
        String sb = "MousePosition{" + "x=" + x +
                ", y=" + y +
                ", isClick=" + isClick +
                '}';
        return sb;
    }
}
