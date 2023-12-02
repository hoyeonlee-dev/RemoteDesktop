package kr.ac.hansung.remoteDesktop.window.event;

import kr.ac.hansung.remoteDesktop.connection.client.ClientSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileDropListenerActionImpl implements FileDropListener.FileDropAction {
    ClientSession clientSession;

    public FileDropListenerActionImpl(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    @Override
    public void fileDropped(File file) {
        var files = new ArrayList<File>();
        files.add(file);
        new Thread(() -> {
            try {
                clientSession.sendFileTransferRequest(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void fileDropped(List<File> files) {
        new Thread(() -> {
            try {
                clientSession.sendFileTransferRequest(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
