package kr.ac.hansung.remoteDesktop.ui.window.event;

import kr.ac.hansung.remoteDesktop.message.content.FileMessage;
import kr.ac.hansung.remoteDesktop.message.content.FileSendRequest;
import kr.ac.hansung.remoteDesktop.server.connection.socketListener.FileSocketListener;
import kr.ac.hansung.remoteDesktop.server.session.SessionManager;
import kr.ac.hansung.remoteDesktop.ui.window.dialog.AskFileTransferDialog;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class OnFileReceivedListener implements FileSocketListener.OnFileReceivedListener {
    JFrame         hostWindow;
    SessionManager sessionManager;

    public OnFileReceivedListener(JFrame hostWindow, SessionManager sessionManager) {
        this.hostWindow     = hostWindow;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean receiveFileTransferRequest(FileSendRequest request) {
        boolean       isUserExists         = false;
        AtomicBoolean fileTransferAccepted = new AtomicBoolean(false);

        for (var session : sessionManager.getSessions()) {
            if (session.getKey().equals(request.key())) {
                isUserExists = true;
                break;
            }
        }

        if (isUserExists) {
            AskFileTransferDialog dialog = makeAskFileTransferDialog(request, fileTransferAccepted);
            dialog.start();
        }

        for (var name : request.fileNames()) {
            System.out.println(name);
        }
        return isUserExists && fileTransferAccepted.get();
    }

    @Override
    public void receiveFile(FileMessage fileMessage) {
        try {
            var fileOutputStream = new FileOutputStream(String.format("./%s", fileMessage.fileName()));
            var content          = new ByteArrayInputStream(fileMessage.content());
            fileOutputStream.write(fileMessage.content());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AskFileTransferDialog makeAskFileTransferDialog(FileSendRequest request, AtomicBoolean fileTransferAccepted) {
        var dialog = new AskFileTransferDialog(hostWindow);
        dialog.setPosition(1000, 400);
        dialog.setDialogSize(500, 300);

        dialog.setDialogText(request.fileNames());

        dialog.setOk(l -> {
            fileTransferAccepted.set(true);
            dialog.dispose();
        });

        dialog.setDeny(l -> {
            fileTransferAccepted.set(false);
            dialog.dispose();
        });
        return dialog;
    }
}
