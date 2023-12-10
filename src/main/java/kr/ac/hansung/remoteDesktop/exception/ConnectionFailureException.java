package kr.ac.hansung.remoteDesktop.exception;

import java.io.IOException;

// 연결과정에서 문제가 발생했을 때 사용하는 Exception
public class ConnectionFailureException extends IOException {

    public static final String ERROR_MESSAGE_BASE = "연결에 실패했습니다. : ";

    public ConnectionFailureException(String message) {
        super(ERROR_MESSAGE_BASE + message);
    }

    public ConnectionFailureException(String message, Throwable cause) {
        super(ERROR_MESSAGE_BASE + message, cause);
    }
}
