package kr.ac.hansung.remoteDesktop.exception;

import java.io.IOException;

// 서버에서 일방적으로 연결을 닫았을 때 클라이언트쪽에서 사용하는 Exception
public class ConnectionClosedByHostException extends IOException {

    public static final String ERROR_MESSAGE_BASE = "호스트가 연결을 종료했습니다. : ";
    private Type type;

    public ConnectionClosedByHostException(String message) {
        super(ERROR_MESSAGE_BASE + message);
    }

    public ConnectionClosedByHostException(Type type, String message, Throwable cause) {
        super(ERROR_MESSAGE_BASE + message, cause);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        DEFAULT(0), HOST_CRASHED(1);
        int value;

        Type(int value) {
            this.value = value;
        }
    }
}
