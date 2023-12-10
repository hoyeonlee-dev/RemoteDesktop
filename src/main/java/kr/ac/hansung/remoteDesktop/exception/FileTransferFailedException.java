package kr.ac.hansung.remoteDesktop.exception;

import java.io.IOException;

// 파일 전송과정에서 문제가 발생했을 때 사용하는 Exception
public class FileTransferFailedException extends IOException {
    public FileTransferFailedException(String message) {
        super(message);
    }

    public FileTransferFailedException(Throwable cause) {
        super(cause);
    }
}
