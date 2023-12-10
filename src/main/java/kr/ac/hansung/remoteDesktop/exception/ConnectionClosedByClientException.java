package kr.ac.hansung.remoteDesktop.exception;

import java.io.IOException;

// 클라이언트에서 일방적으로 연결을 닫았을 때 서버쪽에서 사용하는 Exception
public class ConnectionClosedByClientException extends IOException {
    public static final String ERROR_MESSAGE_BASE = "클라이언트가 연결을 종료했습니다.";
    private Type type;

    public ConnectionClosedByClientException(String message) {
        super(ERROR_MESSAGE_BASE + message);
    }

    public ConnectionClosedByClientException(Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        DEFAULT(0), CLIENT_CRASHED(1);
        int value;

        Type(int value) {
            this.value = value;
        }
    }
}
