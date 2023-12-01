package kr.ac.hansung.remoteDesktop.connection.client;

import kr.ac.hansung.remoteDesktop.connection.Session;
import kr.ac.hansung.remoteDesktop.connection.server.FileServer;
import kr.ac.hansung.remoteDesktop.exception.ConnectionFailureException;
import kr.ac.hansung.remoteDesktop.exception.FileTransferFailedException;
import kr.ac.hansung.remoteDesktop.network.message.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientSession extends Session {
    private String sessionID;

    public ClientSession(String sessionID, Socket controlSocket) {
        this((Socket) null, controlSocket);
        this.sessionID = sessionID;
    }

    private ClientSession(Socket videoSocket, Socket controlSocket) {
        super(videoSocket, controlSocket);
    }

    public boolean requestVideoSocket() throws ConnectionFailureException {
        var host = controlSocket.getInetAddress().getHostAddress();
        try {
            var videoSocket = new Socket(host, Session.VIDEO_PORT);
            videoSocket.setSoTimeout(10_000);
            var writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(videoSocket.getOutputStream())));
            writer.println(sessionID);
            writer.flush();
            this.videoSocket = videoSocket;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            if (controlSocket.isConnected()) {
                try {
                    controlSocket.close();
                } catch (IOException ignored) {
                }
            }
            if (videoSocket.isConnected()) {
                try {
                    videoSocket.close();
                } catch (IOException ignored) {
                }
            }
            videoSocket   = null;
            controlSocket = null;
            throw new ConnectionFailureException("video 연결 실패");
        }
        return true;
    }

    public Socket requestFileSocket() throws ConnectionFailureException {
        var    address    = controlSocket.getInetAddress().getHostAddress();
        Socket fileSocket = null;
        try {
            fileSocket = new Socket(address, FileServer.FILE_PORT);
            fileSocket.setSoTimeout(10_000);
        } catch (IOException e) {
            throw new ConnectionFailureException("파일 소켓 생성에 실패했습니다. ", e);
        }
        return fileSocket;
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
            if (imageInfo.type() == ImageInfo.Type.NO_UPDATE) {
                return 0;
            }
            int length = imageInfo.size();
            if (length == 0) return tmp.length;
            inputStream.readNBytes(tmp, 0, length);
            System.arraycopy(tmp, 0, buffer, 0, length);
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

    public void sendFileTransferRequest(List<File> files) throws FileTransferFailedException {
        try (Socket socket = requestFileSocket();) {
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

    public static class Factory {
        public static ClientSession createClientSession(String address, Supplier<String> passwordSupplier) throws ConnectionFailureException {
            ClientSession clientSession = null;
            try {
                Socket controlSocket = new Socket(address, CONTROL_PORT);
                controlSocket.setSoTimeout(10_000);

                String sessionID = passwordAuthentication(controlSocket, passwordSupplier);

                clientSession = new ClientSession(sessionID, controlSocket);
                clientSession.requestVideoSocket();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                throw new ConnectionFailureException("Connection Failed", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return clientSession;
        }

        private static String passwordAuthentication(Socket controlSocket, Supplier<String> passwordSupplier) throws IOException, ClassNotFoundException {
            var    objectInputStream = new ObjectInputStream(controlSocket.getInputStream());
            var    printWriter       = new PrintWriter(controlSocket.getOutputStream());
            String sessionID         = null;
            while (true) {
                var authMessage = ((AuthMessage) objectInputStream.readObject());
                var messageType = authMessage.type();

                if (messageType == AuthMessage.Type.CONNECTION_RESET) {
                    throw new ConnectionFailureException("호스트에서 접속을 거부했습니다.");
                } else if (messageType == AuthMessage.Type.PASSWORD_NOT_REQUIRED || messageType == AuthMessage.Type.ACCEPTED) {
                    sessionID = authMessage.message();
                    break;
                } else if (messageType == AuthMessage.Type.PASSWORD_WRONG || messageType == AuthMessage.Type.PASSWORD_REQUIRED) {
                    printWriter.println(passwordSupplier.get());
                    printWriter.flush();
                }
            }
            return sessionID;
        }
    }
}
