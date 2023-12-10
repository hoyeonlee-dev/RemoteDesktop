package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 클라이언트 또는 서버가 접속을 종료했을 때 보내는 메시지
public record ConnectionClosed(String message) implements Serializable {
    @Override
    public String toString() {
        String sb = "ConnectionClosed{" + "message='" + message + '\'' +
                '}';
        return sb;
    }
}
