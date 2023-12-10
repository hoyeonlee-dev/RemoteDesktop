package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;
import java.util.List;

// 클라이언트가 서버에 파일을 전송하기에 앞서 파일의 내용들을 담아서 보낼 때 사용하는 메시지
public class FileSendRequest implements Serializable {
    private String key;
    private List<String> fileNames;

    public FileSendRequest(String key, List<String> fileNames) {
        this.key = key;
        this.fileNames = fileNames;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    @Override
    public String toString() {
        String sb = "FileSendRequest{" + "key='" + key + '\'' +
                ", fileNames=" + fileNames +
                '}';
        return sb;
    }
}
