package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

public class KeyEventInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int keyCode;
    private final boolean keyPress;

    public KeyEventInfo(int keyCode, boolean keyPress) {
        this.keyCode = keyCode;
        this.keyPress = keyPress;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isKeyPress() {
        return keyPress;
    }
}