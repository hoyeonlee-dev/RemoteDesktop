package kr.ac.hansung.remoteDesktop.network.message;

import java.io.Serializable;

public class KeyEventInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int keyCode;
    private boolean keyPress;

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