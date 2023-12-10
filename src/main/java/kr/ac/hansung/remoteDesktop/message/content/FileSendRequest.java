package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;
import java.util.List;

// 클라이언트가 서버에 파일을 전송하기에 앞서 파일의 내용들을 담아서 보낼 때 사용하는 메시지
public record FileSendRequest(String key, List<String> fileNames) implements Serializable {
    @Override
    public String toString() {
        String sb = "FileSendRequest{" + "key='" + key + '\'' +
                ", fileNames=" + fileNames +
                '}';
        return sb;
    }
}
