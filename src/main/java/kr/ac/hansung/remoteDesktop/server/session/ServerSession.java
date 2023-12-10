package kr.ac.hansung.remoteDesktop.server.session;


import kr.ac.hansung.remoteDesktop.Settings;
import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.PasswordMessage;
import kr.ac.hansung.remoteDesktop.server.receiver.FileReceiver;
import kr.ac.hansung.remoteDesktop.server.receiver.RemoteInputReceiver;
import kr.ac.hansung.remoteDesktop.server.sender.MessageSender;
import kr.ac.hansung.remoteDesktop.server.sender.VideoSender;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSession extends Session implements Closeable {

    private final String sessionID;
    private MessageSender messageSender;
    private VideoSender videoSender;

    private RemoteInputReceiver remoteInputReceiver;

    private FileReceiver fileReceiver;

    private ObjectOutputStream videoOut;
    private ObjectInputStream controlIn;
    private ObjectOutputStream controlOut;
    private boolean isClosed;

    public ServerSession(String sessionID) {
        super(null, null);
        this.sessionID = sessionID;
        isClosed = false;
    }

    public String getSessionID() {
        return sessionID;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    public VideoSender getVideoSender() {
        return videoSender;
    }

    public FileReceiver getFileReceiver() {
        return fileReceiver;
    }

    public RemoteInputReceiver getRemoteInputReceiver() {
        if (remoteInputReceiver == null) {
            try {
                remoteInputReceiver = new RemoteInputReceiver(controlIn, new Robot());
            } catch (AWTException e) {
            }
        }
        return remoteInputReceiver;
    }

    // 현재 세션에 제어 소켓을 붙입니다.
    public void attachControlSocket(Socket controlSocket) throws IOException {
        super.setControlSocket(controlSocket);
        controlOut = new ObjectOutputStream(controlSocket.getOutputStream());
        messageSender = new MessageSender(controlOut);
    }

    // 현재 세션에 영상 소켓을 붙입니다.
    public void attachVideoSocket(Socket videoSocket) throws IOException {
        super.setVideoSocket(videoSocket);
        videoOut = new ObjectOutputStream(videoSocket.getOutputStream());
        videoSender = new VideoSender(videoOut);
    }

    @Override
    public void close() {
        try {
            messageSender.sendCloseMessage("");
        } catch (IOException e) {
        }
        isClosed = true;
        try {
            videoOut.close();
        } catch (IOException e) {
            System.out.println();
        }
        try {
            controlIn.close();
        } catch (IOException e) {
            System.out.println();
        }
        try {
            controlOut.close();
        } catch (IOException e) {
            System.out.println();
        }
        try {
            if (videoSocket != null) videoSocket.close();
        } catch (IOException e) {
            System.out.println();
        }
        try {
            if (controlSocket != null) controlSocket.close();
        } catch (IOException e) {
            System.out.println();
        }
    }

    /**
     * 서버의 관점에서 비밀번호 인증을 처리하는 메서드
     */
    public boolean passwordAuthentication() throws IOException, ClassNotFoundException {
        if (Settings.getInstance().getPassword().isEmpty()) {
            controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD,
                                                     new PasswordMessage(PasswordMessage.Type.PASSWORD_NOT_REQUIRED,
                                                                         sessionID)));
            if (controlIn == null) controlIn = new ObjectInputStream(controlSocket.getInputStream());
            return true;
        }
        controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD,
                                                 new PasswordMessage(PasswordMessage.Type.PASSWORD_REQUIRED, "")));
        controlOut.flush();
        int count = 0;
        if (controlIn == null) controlIn = new ObjectInputStream(controlSocket.getInputStream());
        for (int i = 0; i <= 3; i++) {
            var message = (RemoteMessage) controlIn.readObject();
            System.out.printf("Server received : %s\n", message);
            if (message.getType() == RemoteMessage.Type.CONNECTION_CLOSED) return false;
            if (message.getType() != RemoteMessage.Type.PASSWORD) continue;
            if (!message.getData().equals(Settings.getInstance().getPassword())) {
                if (++count >= 3) {
                    System.out.println("3회를 초과했습니다 연결을 거절합니다.");
                    controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD,
                                                             new PasswordMessage(PasswordMessage.Type.CONNECTION_RESET,
                                                                                 "")));
                    controlOut.flush();
                    return false;
                } else {
                    System.out.println("암호가 틀렸습니다.");
                    controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD,
                                                             new PasswordMessage(PasswordMessage.Type.PASSWORD_WRONG,
                                                                                 "")));
                    controlOut.flush();
                }
            } else {
                System.out.println("접속을 승인합니다.");
                controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD,
                                                         new PasswordMessage(PasswordMessage.Type.ACCEPTED,
                                                                             sessionID)));
                controlOut.flush();
                break;
            }
        }
        return true;
    }
}
