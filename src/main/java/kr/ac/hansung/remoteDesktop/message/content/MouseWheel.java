package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

public class MouseWheel implements Serializable {
    private final int rotation;

    public MouseWheel(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }
}
