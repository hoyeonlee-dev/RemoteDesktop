package kr.ac.hansung.remoteDesktop;

import javax.swing.*;
import java.io.File;

public class Settings {
	private static Settings instance;
	private static String receivedFilesPath;
	private static String savePath;
	
    public static String password = "asdf";
    
    private Settings() {
        receivedFilesPath = "defaultReceivedFilesPath";
        savePath = "defaultSavePath";
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
        this.savePath = savePath;
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
