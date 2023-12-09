package kr.ac.hansung.remoteDesktop.message;

import java.io.Serializable;

/**
 * 클래스는 서버 및 클라이언트 간의 메시지 전송을 위한 클래스를 정의합니다.
 */
public class RemoteMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Type type;
    private final Serializable data;

    public RemoteMessage(Type type, Serializable data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public Serializable getData() {
        return data;
    }

    public enum Type {
        MOUSE_POSITION,
        MOUSE_CLICK,
        KEYBOARD,
        CONNECTION_CLOSED,
        PASSWORD,
        FILE_SEND_REQUEST,
        FILE_SEND_RESPONSE,
        FILE
    }
}