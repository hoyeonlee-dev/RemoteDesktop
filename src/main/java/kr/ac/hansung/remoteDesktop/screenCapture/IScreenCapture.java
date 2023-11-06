package kr.ac.hansung.remoteDesktop.screenCapture;

import java.awt.image.BufferedImage;

public interface IScreenCapture extends IBufferedCapture {
    void onWindowSizeUpdated();

    BufferedImage createBufferedImage();
}
