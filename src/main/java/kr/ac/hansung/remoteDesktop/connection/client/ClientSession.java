package kr.ac.hansung.remoteDesktop.connection.client;

import kr.ac.hansung.remoteDesktop.connection.Session;
import kr.ac.hansung.remoteDesktop.connection.server.FileServer;
import kr.ac.hansung.remoteDesktop.network.message.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSession extends Session {
    private String sessionID;

    public ClientSession(String sessionID, Socket controlSocket) {
        this((Socket) null, controlSocket);
        this.sessionID = sessionID;
    }

    private ClientSession(Socket videoSocket, Socket controlSocket) {
        super(videoSocket, controlSocket);
    }

    public boolean requestVideoSocket() {
        var host        = controlSocket.getInetAddress().getHostAddress();
        var videoSocket = new Socket();
        try {
            videoSocket.connect(new InetSocketAddress(InetAddress.getByName(host), Session.VIDEO_PORT));
            var writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(videoSocket.getOutputStream())));
            writer.println(sessionID);
            writer.flush();
            this.videoSocket = videoSocket;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            if (videoSocket.isConnected()) {
                try {
                    videoSocket.close();
                } catch (IOException ignored) {
                }
            }
            videoSocket = null;
            return false;
        }
        return true;
    }

    byte[] tmp = new byte[20 * 1024 * 1024];

    ObjectInputStream inputStream = null;

    public int receiveVideo(byte[] buffer) {
        if (videoSocket == null) return -1;
        if (videoSocket.isClosed()) return -1;
        try {
            if (inputStream == null)
                inputStream = new ObjectInputStream(videoSocket.getInputStream());
            var imageInfo = (ImageInfo) inputStream.readObject();
            if (imageInfo.imageType() == ImageType.NO_UPDATE) {
                return 0;
            }
            int length = imageInfo.size();
            if (length == 0) return tmp.length;
            inputStream.readNBytes(tmp, 0, length);
            System.arraycopy(tmp, 0, buffer, 0, length);
//            System.out.printf("client: %d \n", length);
            return length;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return -1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    ObjectInputStream controlObjectInputStream = null;

    public MousePosition receiveMousePosition() {
        try {
            if (controlObjectInputStream == null) controlObjectInputStream = new ObjectInputStream(controlSocket.getInputStream());
            return (MousePosition) controlObjectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public Socket requestFileSocket() throws IOException {
        var address = controlSocket.getInetAddress().getHostAddress();
        return new Socket(address, FileServer.FILE_PORT);
    }

    public void sendFileTransferRequest(List<File> files) throws IOException {
        var socket       = requestFileSocket();
        var inputStream  = socket.getInputStream();
        var outputStream = socket.getOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        ObjectInputStream  objectInputStream  = new ObjectInputStream(inputStream);

        var names = new ArrayList<String>();

        for (var file : files) {
            names.add(file.getName());
        }
        objectOutputStream.writeObject(new FileSendRequest(sessionID, names));
        objectOutputStream.flush();
        try {
            var response = (FileSendResponse) objectInputStream.readObject();
            if (response.type() == FileSendResponse.Type.OK) {
                System.out.println("서버에서 파일 전송을 승인했습니다.");
                sendFiles(objectOutputStream, files);
            } else if (response.type() == FileSendResponse.Type.DENIED) {
                System.out.println("서버에서 파일 전송을 거절했습니다..");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendFiles(ObjectOutputStream objectOutputStream, List<File> files) {
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
                e.printStackTrace();
            }
        }
    }

    public static class Factory {
        public static ClientSession createClientSession(String address) {
            ClientSession clientSession = null;
            try {
                Socket controlSocket = new Socket(address, CONTROL_PORT);
                var    reader        = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                clientSession = new ClientSession(reader.readLine(), controlSocket);
                while (!clientSession.requestVideoSocket()) {
                }

            } catch (IOException e) {
                System.err.println(e.getMessage());
                return null;
            }
            return clientSession;
        }
    }

}
