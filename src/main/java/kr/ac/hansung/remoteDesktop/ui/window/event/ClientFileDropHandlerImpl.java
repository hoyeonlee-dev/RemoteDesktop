package kr.ac.hansung.remoteDesktop.ui.window.event;

import kr.ac.hansung.remoteDesktop.client.connection.ClientSession;
import kr.ac.hansung.remoteDesktop.client.sender.FileSender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientFileDropHandlerImpl implements FileDropHandler {
    ClientSession clientSession;

    public ClientFileDropHandlerImpl(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    @Override
    public void fileDropped(File file) {
        var files = new ArrayList<File>();
        files.add(file);
        new Thread(() -> {
            try {
                var sender = new FileSender(clientSession);
                sender.sendFiles(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void fileDropped(List<File> files) {
        new Thread(() -> {
            try {
                var sender = new FileSender(clientSession);
                sender.sendFiles(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
