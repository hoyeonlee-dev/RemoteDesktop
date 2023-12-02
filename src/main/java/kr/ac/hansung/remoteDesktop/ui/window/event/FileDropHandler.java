package kr.ac.hansung.remoteDesktop.ui.window.event;

import java.io.File;
import java.util.List;

public interface FileDropHandler {
    void fileDropped(File file);

    void fileDropped(List<File> files);
}
