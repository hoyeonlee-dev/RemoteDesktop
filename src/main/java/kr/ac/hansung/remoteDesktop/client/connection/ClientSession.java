package kr.ac.hansung.remoteDesktop.client.connection;

import kr.ac.hansung.remoteDesktop.client.receiver.MessageReceiver;
import kr.ac.hansung.remoteDesktop.client.receiver.VideoReceiver;
import kr.ac.hansung.remoteDesktop.client.sender.RemoteKeySender;
import kr.ac.hansung.remoteDesktop.client.sender.RemoteMouseSender;
import kr.ac.hansung.remoteDesktop.exception.ConnectionFailureException;
import kr.ac.hansung.remoteDesktop.message.RemoteMessage;
import kr.ac.hansung.remoteDesktop.message.content.ConnectionClosed;
import kr.ac.hansung.remoteDesktop.message.content.Empty;
import kr.ac.hansung.remoteDesktop.message.content.PasswordMessage;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.FileSocketListener;
import kr.ac.hansung.remoteDesktop.server.session.Session;
import kr.ac.hansung.remoteDesktop.ui.window.dialog.AskPasswordDialog;

import java.io.*;
import java.net.Socket;
import java.util.function.Function;

/**
 * 원격 데스크톱 클라이언트가 서버와 상호작용을 위해 사용하는 클래스
 * 영상, 제어, 파일과 관련된 멤버를 사용하여 서버와 상호작용합니다.
 * 각 멤버들은 이 클래스는 Composition 관계입니다.
 */
public class ClientSession implements Closeable {

    // 입출력을 담당하는 소켓과 스트림
    private final Socket controlSocket;
    // 원격 세션의 기본 정의
    private String sessionID;
    private String address;
    // 메시지를 전송하는 멤버들
    private VideoReceiver videoReceiver;
    private MessageReceiver messageReceiver;
    private RemoteKeySender remoteKeySender;
    private RemoteMouseSender remoteMouseSender;
    private Socket videoSocket;
    private ObjectInputStream controlIn;
    private ObjectOutputStream controlOut;

    private boolean isClosed;


    /**
     * 클라이언트 소켓을 생성합니다.
     *
     * @param controlSocket 연결된 제어소켓
     */
    private ClientSession(Socket controlSocket) {
        this.controlSocket = controlSocket;
        isClosed = false;
    }

    /**
     * 클라이언트 소켓을 초기화 합니다.
     *
     * @throws ConnectionFailureException 영상소켓 또는 각종 핸들러의 초기화에 실패했을 때
     */
    public void init() throws ConnectionFailureException {
        address = controlSocket.getInetAddress().getHostAddress();
        this.videoSocket = createVideoSocket();

        remoteKeySender = new RemoteKeySender(controlOut);
        remoteMouseSender = new RemoteMouseSender(controlOut);
        messageReceiver = new MessageReceiver(controlIn);
        videoReceiver = new VideoReceiver(videoSocket);
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

    /**
     * 이 ClientSession을 종료하는 메서드
     *
     * @throws IOException 사실상 무시해도 되는 Exception
     */
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

    /**
     * 호스트에게 영상 수신을 위한 소켓을 요청
     *
     * @return 영상을 수신할 소켓
     * @throws ConnectionFailureException 소켓 생성에 실패한 경우
     */
    private Socket createVideoSocket() throws ConnectionFailureException {
        try {
            var videoSocket = new Socket(address, Session.VIDEO_PORT);
            videoSocket.setSoTimeout(10_000);
            var writer = new PrintWriter(
                    new OutputStreamWriter(new BufferedOutputStream(videoSocket.getOutputStream())));
            writer.println(sessionID);
            writer.flush();
            return videoSocket;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new ConnectionFailureException("video 연결 실패");
        }
    }

    /**
     * 호스트에 파일 전송을 하기 위해 새로운 소켓을 요청
     *
     * @return 소켓 생성에 성공한 경우
     * @throws ConnectionFailureException 소켓 생성에 실패한 경우
     */
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

    /**
     * 비밀번호 인증과정을 다루는 메서드
     *
     * @param controlSocket    클라이언트의 제어소켓
     * @param passwordSupplier 비밀번호 입력을 처리하는 메서드
     * @throws IOException            메시지 송수신 중 문제가 발생한 경우
     * @throws ClassNotFoundException 비밀번호 인증 또는 연결종료가 아닌 메시지를 수신했을 경우
     */
    private void passwordAuthentication(Socket controlSocket, Function<AskPasswordDialog.Type, String> passwordSupplier) throws IOException, ClassNotFoundException {
        if (controlOut == null)
            controlOut = new ObjectOutputStream(controlSocket.getOutputStream());
        if (controlIn == null)
            controlIn = new ObjectInputStream(controlSocket.getInputStream());
        String sessionID = null;
        while (true) {
            var message = (RemoteMessage) controlIn.readObject();
            var authMessage = ((PasswordMessage) message.getData());
            var messageType = authMessage.type();
            System.out.printf("Client auth : %s\n", message);

            if (messageType == PasswordMessage.Type.PASSWORD_REQUIRED || messageType == PasswordMessage.Type.PASSWORD_WRONG) {
                String passwordInput = passwordSupplier.apply(AskPasswordDialog.Type.FIRST);
                System.out.println("암호 입력이 필요합니다.");
                if (passwordInput == null) {
                    controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.CONNECTION_CLOSED, new Empty()));
                    controlOut.flush();
                    return;
                } else {
                    controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, passwordInput));
                    controlOut.flush();
                }
            } else if (messageType == PasswordMessage.Type.PASSWORD_NOT_REQUIRED || messageType == PasswordMessage.Type.ACCEPTED) {
                System.out.println("암호가 필요없습니다.");
                controlOut.writeObject(new RemoteMessage(RemoteMessage.Type.PASSWORD, new Empty()));
                controlOut.flush();
                sessionID = authMessage.message();
                break;
            } else if (messageType == PasswordMessage.Type.CONNECTION_RESET) {
                System.out.println("호스트에서 접속을 거부했습니다.");
                throw new ConnectionFailureException("호스트에서 접속을 거부했습니다.");
            }
        }
        this.sessionID = sessionID;
    }


    public static class Factory {
        /**
         * 클라이언트 세션 생성과정이 복잡하기 때문에 생성과정을 별도의 정적 메서드로 분리했음
         *
         * @param address          접속할 원격 컴퓨터의 주소
         * @param passwordSupplier 원격 컴퓨터가 암호를 요구할 경우 사용자에게 암호를 요청할 메서드
         * @return 원격 클라이언트 세션
         * @throws ConnectionFailureException 접속에 실패한 경우
         */
        public static ClientSession createClientSession(String address, Function<AskPasswordDialog.Type, String> passwordSupplier) throws ConnectionFailureException {
            ClientSession clientSession = null;
            try {
                Socket controlSocket = new Socket(address, Session.CONTROL_PORT);
                controlSocket.setSoTimeout(10_000);

                clientSession = new ClientSession(controlSocket);
                clientSession.passwordAuthentication(controlSocket, passwordSupplier);
                clientSession.init();

            } catch (IOException e) {
                System.err.printf("연결에 실패했습니다. %s\n", e.getMessage());
                throw new ConnectionFailureException("Connection Failed", e);
            } catch (ClassNotFoundException e) {
                System.err.printf("개발자가 확인해야 하는 문제입니다. %s\n", e.getMessage());
                throw new ConnectionFailureException("Connection Failed", e);
            }
            return clientSession;
        }


    }
}
