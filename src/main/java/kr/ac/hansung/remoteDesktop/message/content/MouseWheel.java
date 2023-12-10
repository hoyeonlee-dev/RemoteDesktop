package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 마우스 휠의 상태를 담는 메시지(동작하지 않음)
public class MouseWheel implements Serializable {
    private final int rotation;

    public MouseWheel(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        String sb = "MouseWheel{" + "rotation=" + rotation +
                '}';
        return sb;
    }
}
