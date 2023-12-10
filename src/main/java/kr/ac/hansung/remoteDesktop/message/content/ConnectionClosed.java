package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 클라이언트 또는 서버가 접속을 종료했을 때 보내는 메시지
public class ConnectionClosed implements Serializable {
    private String message;

    public ConnectionClosed(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        String sb = "ConnectionClosed{" + "message='" + message + '\'' +
                '}';
        return sb;
    }
}
