package kr.ac.hansung;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class BufferedWrapper implements Serializable {
    BufferedImage bufferedImage;

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public BufferedWrapper(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
