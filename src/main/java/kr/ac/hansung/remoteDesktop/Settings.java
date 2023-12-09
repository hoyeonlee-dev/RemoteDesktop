package kr.ac.hansung.remoteDesktop;

import javax.swing.*;
import java.io.File;

public class Settings {
    private String password = "";
    private boolean allowClientInput = true;
    private boolean allowHosting = true;

    private static String receivedFilesPath;
    private static String savePath;


    private Settings() {
        receivedFilesPath = System.getProperty("user.dir");
        savePath = System.getProperty("user.dir");
    }

    public static Settings getInstance() {
        return InstanceHolder.getInstance();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAllowClientInput() {
        return allowClientInput;
    }

    public void setAllowClientInput(boolean allowClientInput) {
        this.allowClientInput = allowClientInput;
    }

    public boolean isAllowHosting() {
        return allowHosting;
    }

    public void setAllowHosting(boolean allowHosting) {
        this.allowHosting = allowHosting;
    }

    private static class InstanceHolder {
        private static final Settings instance = new Settings();

        public static Settings getInstance() {
            return instance;
        }
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
