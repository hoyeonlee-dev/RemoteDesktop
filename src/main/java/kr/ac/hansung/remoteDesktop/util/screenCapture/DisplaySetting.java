package kr.ac.hansung.remoteDesktop.util.screenCapture;

import kr.ac.hansung.remoteDesktop.util.DLLLoader;

/**
 * 디스플레이 설정을 조작할 때 사용하는 클래스
 */
@SuppressWarnings("FieldMayBeFinal")
public class DisplaySetting {
    private final static String LIBRARY_NAME = "DisplaySetting.dll";

    static {
        DLLLoader.LoadDLL(LIBRARY_NAME);
    }

    private int savedWidth = 0;
    private int savedHeight = 0;
    private int savedRefreshRate = 0;

    public static void main(String[] args) throws InterruptedException {
        var setting = new DisplaySetting();
        setting.backupDisplaySettings();
        System.out.printf("width : %d, height : %d\n", setting.savedWidth, setting.savedHeight);
        setting.nativeResize(1920, 1080, 60);
        Thread.sleep(20000);
        setting.resize(setting.savedWidth, setting.savedHeight, setting.savedRefreshRate);
    }

    private native void nativeBackupDisplaySettings();

    private native void nativeResize(int width, int height, int refreshRate);

    public void resize(int width, int height, int refreshRate) {
        nativeResize(width, height, refreshRate);
    }

    public void restore() {
        nativeResize(savedWidth, savedHeight, savedRefreshRate);
    }

    public void backupDisplaySettings() {
        nativeBackupDisplaySettings();
    }
}
