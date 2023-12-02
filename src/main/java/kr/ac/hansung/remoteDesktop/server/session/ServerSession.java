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

    private final String        sessionID;
    private       MessageSender messageSender;
    private       VideoSender   videoSender;

    private RemoteInputReceiver remoteInputReceiver;

    private FileReceiver fileReceiver;

    private ObjectOutputStream videoOut;
    private ObjectInputStream  controlIn;
    private ObjectOutputStream controlOut;
    private boolean            isClosed;

    public ServerSession(String sessionID) {
        super(null, null);
        this.sessionID = sessionID;
        isClosed       = false;
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

    public void attachControlSocket(Socket controlSocket) throws IOException {
        super.setControlSocket(controlSocket);
        controlOut    = new ObjectOutputStream(controlSocket.getOutputStream());
        messageSender = new MessageSender(controlOut);
    }

    public void attachVideoSocket(Socket videoSocket) throws IOException {
        super.setVideoSocket(videoSocket);
        videoOut    = new ObjectOutputStream(videoSocket.getOutputStream());
        videoSender = new VideoSender(videoOut);
    }

    @Override
    public void close() {
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

    public boolean passwordAuthentication() throws IOException, ClassNotFoundException {
        if (Settings.password.isEmpty())
            controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, new PasswordMessage(PasswordMessage.Type.PASSWORD_NOT_REQUIRED, sessionID)));
        controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, new PasswordMessage(PasswordMessage.Type.PASSWORD_REQUIRED, "")));
        controlOut.flush();
        int count = 0;
        if (controlIn == null) controlIn = new ObjectInputStream(controlSocket.getInputStream());
        for (int i = 0; i <= 3; i++) {
            var message = (RemoteMessage) controlIn.readObject();
            if (message.getType() != RemoteMessage.Type.PASSWORD) continue;
            if (!message.getData().equals(Settings.password)) {
                if (++count >= 3) {
                    controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, new PasswordMessage(PasswordMessage.Type.CONNECTION_RESET, "")));
                    controlOut.flush();
                    return false;
                } else {
                    controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, new PasswordMessage(PasswordMessage.Type.PASSWORD_WRONG, "")));
                    controlOut.flush();
                }
            } else {
                controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, new PasswordMessage(PasswordMessage.Type.ACCEPTED, sessionID)));
                controlOut.flush();
                break;
            }
        }
        return true;
    }
}
