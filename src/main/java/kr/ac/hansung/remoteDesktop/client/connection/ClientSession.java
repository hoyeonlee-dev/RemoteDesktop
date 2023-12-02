package kr.ac.hansung.remoteDesktop.client.connection;

import kr.ac.hansung.remoteDesktop.client.receiver.MessageReceiver;
import kr.ac.hansung.remoteDesktop.client.receiver.VideoReceiver;
import kr.ac.hansung.remoteDesktop.client.sender.RemoteKeySender;
import kr.ac.hansung.remoteDesktop.client.sender.RemoteMouseSender;
import kr.ac.hansung.remoteDesktop.exception.ConnectionFailureException;
import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.ConnectionClosed;
import kr.ac.hansung.remoteDesktop.message.content.PasswordMessage;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.FileSocketListener;
import kr.ac.hansung.remoteDesktop.server.session.Session;
import kr.ac.hansung.remoteDesktop.ui.window.dialog.AskPasswordDialog;

import java.io.*;
import java.net.Socket;
import java.util.function.Function;

public class ClientSession implements Closeable {
    public static final int                EOF = -100;
    private final       Socket             controlSocket;
    private             String             sessionID;
    private             String             address;
    private             VideoReceiver      videoReceiver;
    private             MessageReceiver    messageReceiver;
    private             RemoteKeySender    remoteKeySender;
    private             RemoteMouseSender  remoteMouseSender;
    private             Socket             videoSocket;
    private             ObjectInputStream  controlIn;
    private             ObjectOutputStream controlOut;

    private boolean isClosed;


    public ClientSession(Socket controlSocket) throws IOException {
        this.controlSocket = controlSocket;
        isClosed           = false;
    }

    public void init() throws ConnectionFailureException {
        address          = controlSocket.getInetAddress().getHostAddress();
        this.videoSocket = createVideoSocket();

        remoteKeySender   = new RemoteKeySender(controlOut);
        remoteMouseSender = new RemoteMouseSender(controlOut);
        messageReceiver   = new MessageReceiver(controlIn);
        videoReceiver     = new VideoReceiver(videoSocket);
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public VideoReceiver getVideoReceiver() {
        return videoReceiver;
    }

    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    public RemoteKeySender getKeySender() {
        return remoteKeySender;
    }

    public RemoteMouseSender getMouseSender() {
        return remoteMouseSender;
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.CONNECTION_CLOSED, new ConnectionClosed("")));
        controlOut.flush();

        videoReceiver.close();
        messageReceiver.close();
        remoteKeySender.close();
        remoteMouseSender.close();

        controlSocket.close();
        videoSocket.close();
    }

    private Socket createVideoSocket() throws ConnectionFailureException {
        try {
            var videoSocket = new Socket(address, Session.VIDEO_PORT);
            videoSocket.setSoTimeout(10_000);
            var writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(videoSocket.getOutputStream())));
            writer.println(sessionID);
            writer.flush();
            return videoSocket;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new ConnectionFailureException("video 연결 실패");
        }
    }

    public Socket createFileSocket() throws ConnectionFailureException {
        Socket fileSocket = null;
        try {
            fileSocket = new Socket(address, FileSocketListener.FILE_PORT);
            fileSocket.setSoTimeout(10_000);
        } catch (IOException e) {
            throw new ConnectionFailureException("파일 소켓 생성에 실패했습니다. ", e);
        }
        return fileSocket;
    }

    private void passwordAuthentication(Socket controlSocket, Function<AskPasswordDialog.Type, String> passwordSupplier) throws IOException, ClassNotFoundException {
        if (controlOut == null)
            controlOut = new ObjectOutputStream(controlSocket.getOutputStream());
        if (controlIn == null)
            controlIn = new ObjectInputStream(controlSocket.getInputStream());
        String sessionID = null;
        while (true) {
            var message     = (RemoteMessage) controlIn.readObject();
            var authMessage = ((PasswordMessage) message.getData());
            var messageType = authMessage.type();

            if (messageType == PasswordMessage.Type.PASSWORD_REQUIRED) {
                controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, passwordSupplier.apply(AskPasswordDialog.Type.FIRST)));
                controlOut.flush();
            } else if (messageType == PasswordMessage.Type.PASSWORD_NOT_REQUIRED || messageType == PasswordMessage.Type.ACCEPTED) {
                sessionID = authMessage.message();
                break;
            } else if (messageType == PasswordMessage.Type.CONNECTION_RESET) {
                throw new ConnectionFailureException("호스트에서 접속을 거부했습니다.");
            } else if (messageType == PasswordMessage.Type.PASSWORD_WRONG) {
                controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, passwordSupplier.apply(AskPasswordDialog.Type.FIRST)));
                controlOut.flush();
            }
        }
        this.sessionID = sessionID;
    }


    public static class Factory {
        public static ClientSession createClientSession(String address, Function<AskPasswordDialog.Type, String> passwordSupplier) throws ConnectionFailureException {
            ClientSession clientSession = null;
            try {
                Socket controlSocket = new Socket(address, Session.CONTROL_PORT);
                controlSocket.setSoTimeout(10_000);

                clientSession = new ClientSession(controlSocket);
                clientSession.passwordAuthentication(controlSocket, passwordSupplier);
                clientSession.init();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                throw new ConnectionFailureException("Connection Failed", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return clientSession;
        }


    }
}
