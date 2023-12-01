package kr.ac.hansung.remoteDesktop.exception;

import java.io.IOException;

public class ConnectionTimeoutException extends IOException {

    public static final String ERROR_MESSAGE_BASE = "연결 제한시간을 초과했습니다. : ";

    public ConnectionTimeoutException(String message) {
        super(ERROR_MESSAGE_BASE + message);
    }
}
