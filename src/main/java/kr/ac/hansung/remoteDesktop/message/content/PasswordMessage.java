package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 접속할 때 암호 인증에 사용하는 메시지
public record PasswordMessage(Type type, String message) implements Serializable {
    public enum Type {
        PASSWORD_REQUIRED(0),       // 비밀번호 인증이 필요한 경우
        PASSWORD_NOT_REQUIRED(1),   // 비밀번호 인증이 필요하지 않은 경우
        PASSWORD_WRONG(2),          // 클라이언트가 입력한 비밀번호가 틀린 경우
        CONNECTION_RESET(3),        // 비밀번호가 3회 틀려서 접속을 끊을 경우
        ACCEPTED(4);                // 비밀번호가 맞는 경우
        int value;

        Type(int value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        String sb = "PasswordMessage{" + "type=" + type +
                ", message='" + message + '\'' +
                '}';
        return sb;
    }
}
