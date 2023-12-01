package kr.ac.hansung.remoteDesktop.network.message;

import java.io.Serializable;

public class MouseEventInfo implements Serializable {
    public static final int MOUSE_CLICK = 1;

    private int eventType;
    private int button;
    private boolean pressed;

    public MouseEventInfo(int eventType, int button, boolean pressed) {
        this.eventType = eventType;
        this.button = button;
        this.pressed = pressed;
    }

    public int getEventType() {
        return eventType;
    }

    public int getButton() {
        return button;
    }

    public boolean isPressed() {
        return pressed;
    }
}
