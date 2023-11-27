package kr.ac.hansung.remoteDesktop.window;

import java.io.Serializable;

public class CustomKeyMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private int keyCode;
    private boolean isPressed;

    public CustomKeyMessage(int keyCode, boolean isPressed) {
        this.keyCode = keyCode;
        this.isPressed = isPressed;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isPressed() {
        return isPressed;
    }
}
