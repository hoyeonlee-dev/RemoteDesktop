package kr.ac.hansung.remoteDesktop.server.connection.socketListener;


import kr.ac.hansung.remoteDesktop.message.content.FileMessage;
import kr.ac.hansung.remoteDesktop.message.content.FileSendRequest;
import kr.ac.hansung.remoteDesktop.message.content.FileSendResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

public class FileSocketListener implements Runnable {
    public final static int FILE_PORT = 1237;
    ServerSocket serverSocket;

    OnFileReceivedListener listener;

    public interface OnFileReceivedListener {
        boolean receiveFileTransferRequest(FileSendRequest fileSendRequest);

        void receiveFile(FileMessage fileMessage);
    }

    public OnFileReceivedListener getListener() {
        return listener;
    }

    public void setListener(OnFileReceivedListener listener) {
        this.listener = listener;
    }

    public FileSocketListener(OnFileReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(FILE_PORT);
            while (true) {
                var accepted = serverSocket.accept();
                new Thread(() -> {
                    try {
                        var inputStream  = new ObjectInputStream(accepted.getInputStream());
                        var outputStream = new ObjectOutputStream(accepted.getOutputStream());
                        var fileRequest  = inputStream.readObject();
                        if (fileRequest instanceof FileSendRequest castedRequest) {
                            if (listener.receiveFileTransferRequest(castedRequest)) {
                                // 사용자가 파일 수신을 수락한 경우
                                outputStream.writeObject(new FileSendResponse(FileSendResponse.Type.OK));
                                for (int i = 0; i < castedRequest.fileNames().size(); ++i) {
                                    listener.receiveFile((FileMessage) inputStream.readObject());
                                }
                            } else {
                                // 사용자가 파일 수신을 거절한 경우
                                outputStream.writeObject(new FileSendResponse(FileSendResponse.Type.DENIED));
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {

        }
    }
}