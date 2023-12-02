package kr.ac.hansung.remoteDesktop.ui.window.event;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileDropListener implements java.awt.dnd.DropTargetListener {

    private final List<FileDropHandler> fileDropHandlers;

    public FileDropListener() {
        this.fileDropHandlers = new ArrayList<>();
    }

    public void addFileDropAction(FileDropHandler fileDropHandler) {
        fileDropHandlers.add(fileDropHandler);
    }

    public void removeFileDropAction(FileDropHandler fileDropHandler) {
        fileDropHandlers.remove(fileDropHandler);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        // @DoNothing
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        // @DoNothing
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // @DoNothing
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // @DoNothing
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        event.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable transferable = event.getTransferable();
        DataFlavor[] flavors      = transferable.getTransferDataFlavors();
        if (fileDropHandlers.size() == 0) {
            event.dropComplete(true);
            return;
        }
        for (DataFlavor flavor : flavors) {
            if (flavor.isFlavorJavaFileListType()) {
                List<File> files = null;
                try {
                    files = (List<File>) transferable.getTransferData(flavor);
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!files.isEmpty()) {
                    if (files.size() == 1) {
                        for (var fileDropAction : fileDropHandlers) fileDropAction.fileDropped(files.get(0));
                    } else if (files.size() >= 2) {
                        for (var fileDropAction : fileDropHandlers) fileDropAction.fileDropped(files);
                    }
                }
            }
        }
        event.dropComplete(true);
    }

    private void showFileInfos(File file) {
        if (!file.exists()) {
            System.err.printf("%s 존재하지 않는 파일입니다.\n", file.getAbsolutePath());
            return;
        }
        System.out.printf("%s : %f MB\n", file.getAbsolutePath(), (double) file.length() / 1024 / 1024);
    }
}
