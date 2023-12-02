package kr.ac.hansung.remoteDesktop.server.receiver;

import kr.ac.hansung.remoteDesktop.message.content.FileMessage;
import kr.ac.hansung.remoteDesktop.message.content.FileSendRequest;

import java.util.function.Consumer;


public class FileReceiver {

    private Consumer<FileSendRequest> onFileSendRequestReceived;
    private Consumer<FileMessage>     onFileMessageReceived;

    public Consumer<FileSendRequest> getOnFileSendRequestReceived() {
        return onFileSendRequestReceived;
    }

    public void setOnFileSendRequestReceived(Consumer<FileSendRequest> onFileSendRequestReceived) {
        this.onFileSendRequestReceived = onFileSendRequestReceived;
    }

    public Consumer<FileMessage> getOnFileMessageReceived() {
        return onFileMessageReceived;
    }

    public void setOnFileMessageReceived(Consumer<FileMessage> onFileMessageReceived) {
        this.onFileMessageReceived = onFileMessageReceived;
    }

    public void onFileSendRequest(FileSendRequest message) {
        if (onFileSendRequestReceived != null)
            onFileSendRequestReceived.accept(message);
    }

    public void onFileReceived(FileMessage message) {
        if (onFileMessageReceived != null)
            onFileMessageReceived.accept(message);
    }
}
