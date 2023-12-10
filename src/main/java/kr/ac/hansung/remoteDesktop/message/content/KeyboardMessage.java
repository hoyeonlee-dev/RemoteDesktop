package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 클라이언트의 키보드 입력을 담은 메시지
public class KeyboardMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int keyCode;
    private final boolean isPressed;

    public KeyboardMessage(int keyCode, boolean isPressed) {
        this.keyCode = keyCode;
        this.isPressed = isPressed;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isPressed() {
        return isPressed;
    }

    @Override
    public String toString() {
        String sb = "KeyboardMessage{" + "keyCode=" + keyCode +
                ", isPressed=" + isPressed +
                '}';
        return sb;
    }
}
