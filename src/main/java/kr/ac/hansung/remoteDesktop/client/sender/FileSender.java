package kr.ac.hansung.remoteDesktop.client.sender;

import kr.ac.hansung.remoteDesktop.client.connection.ClientSession;
import kr.ac.hansung.remoteDesktop.exception.FileTransferFailedException;
import kr.ac.hansung.remoteDesktop.message.content.FileMessage;
import kr.ac.hansung.remoteDesktop.message.content.FileSendRequest;
import kr.ac.hansung.remoteDesktop.message.content.FileSendResponse;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 파일 전송을 담당하는 클래스
 */
public class FileSender {
    private final ClientSession clientSession;

    public FileSender(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    public void sendFiles(List<File> files) throws FileTransferFailedException {
        try (Socket socket = clientSession.createFileSocket()) {
            var inputStream = socket.getInputStream();
            var outputStream = socket.getOutputStream();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            var names = new ArrayList<String>();

            for (var file : files) {
                names.add(file.getName());
            }

            objectOutputStream.writeObject(new FileSendRequest(clientSession.getSessionID(), names));
            objectOutputStream.flush();

            try {
                var response = (FileSendResponse) objectInputStream.readObject();
                if (response.getType() == FileSendResponse.Type.OK) {
                    System.out.println("서버에서 파일 전송을 승인했습니다.");
                    sendFiles(objectOutputStream, files);
                } else if (response.getType() == FileSendResponse.Type.DENIED) {
                    System.out.println("서버에서 파일 전송을 거절했습니다..");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new FileTransferFailedException(e);
        }
    }

    private void sendFiles(ObjectOutputStream objectOutputStream, List<File> files) throws FileTransferFailedException {
        for (var file : files) {
            try {
                var fileInputStream = new FileInputStream(file);
                if (!file.exists()) {
                    objectOutputStream.writeObject(new FileMessage(file.getName(), new byte[10]));
                    continue;
                }
                objectOutputStream.writeObject(new FileMessage(file.getName(), fileInputStream.readAllBytes()));
                objectOutputStream.flush();
            } catch (IOException e) {
                throw new FileTransferFailedException(e);
            }
        }
    }
}
