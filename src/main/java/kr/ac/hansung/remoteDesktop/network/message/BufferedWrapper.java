package kr.ac.hansung.remoteDesktop.network.message;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class BufferedWrapper implements Serializable {
    BufferedImage bufferedImage;

    public BufferedWrapper(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
