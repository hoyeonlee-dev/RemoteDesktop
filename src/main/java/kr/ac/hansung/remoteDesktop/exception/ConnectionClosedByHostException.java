package kr.ac.hansung.remoteDesktop.exception;

import java.io.IOException;

public class ConnectionClosedByHostException extends IOException {

    public enum Type {
        DEFAULT(0), HOST_CRASHED(1);
        int value;

        Type(int value) {
            this.value = value;
        }
    }

    private Type type;

    public static final String ERROR_MESSAGE_BASE = "호스트가 연결을 종료했습니다. : ";

    public ConnectionClosedByHostException(String message) {
        super(ERROR_MESSAGE_BASE + message);
    }

    public ConnectionClosedByHostException(Type type, String message) {
        this(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
