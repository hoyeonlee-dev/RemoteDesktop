package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 파일 전송요청이 끝난 뒤에 실제 파일의 내용을 담은 메시지
public class FileMessage implements Serializable {
    private String fileName;
    private byte[] content;

    public FileMessage(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        String sb = "FileMessage{" + "fileName='" + fileName + '\'' +
                ", content=" + (float) content.length / 1024 / 1024 +
                "MB}";
        return sb;
    }
}
