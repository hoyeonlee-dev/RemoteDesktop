package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 파일 전송요청이 끝난 뒤에 실제 파일의 내용을 담은 메시지
public record FileMessage(String fileName, byte[] content) implements Serializable {
    @Override
    public String toString() {
        String sb = "FileMessage{" + "fileName='" + fileName + '\'' +
                ", content=" + (float) content.length / 1024 / 1024 +
                "MB}";
        return sb;
    }
}
