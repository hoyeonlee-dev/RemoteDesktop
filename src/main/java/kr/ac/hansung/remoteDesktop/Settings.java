package kr.ac.hansung.remoteDesktop;

import javax.swing.*;
import java.io.File;

public class Settings {
    public static String password = "1234";
    private static Settings instance;
    private static String receivedFilesPath;
    private static String savePath;

    private Settings() {
        receivedFilesPath = System.getProperty("user.dir");
        savePath = System.getProperty("user.dir");
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        Settings.savePath = savePath;
    }

    public String getReceivedFilesPath() {
        return receivedFilesPath;
    }

    public void setReceivedFilesPath(String path) {
        receivedFilesPath = path;
    }

    public void chooseReceivedFilesPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            setReceivedFilesPath(selectedDirectory.getAbsolutePath());
        }
    }

    public void chooseSavePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            setSavePath(selectedDirectory.getAbsolutePath());
        }
    }
}
