package kr.ac.hansung.remoteDesktop.exception;

import java.io.IOException;

public class FileTransferFailedException extends IOException {
    public FileTransferFailedException(String message) {
        super(message);
    }

    public FileTransferFailedException(Throwable cause) {
        super(cause);
    }
}
