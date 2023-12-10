package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 클라이언트의 마우스 버튼 클릭을 담은 메시지
public class MouseClick implements Serializable {
    public static final int LEFT_BUTTON = 1;
    public static final int RIGHT_BUTTON = 3;

    private int keyCode;
    private boolean isPressed;

    public MouseClick(int keyCode, boolean isPressed) {
        this.keyCode = keyCode;
        this.isPressed = isPressed;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean pressed) {
        isPressed = pressed;
    }

    @Override
    public String toString() {
        String sb = "MouseClick{" + "keyCode=" + keyCode +
                ", isPressed=" + isPressed +
                '}';
        return sb;
    }
}
