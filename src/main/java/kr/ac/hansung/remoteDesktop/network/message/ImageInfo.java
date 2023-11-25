package kr.ac.hansung.remoteDesktop.network.message;

import java.io.Serializable;

public record ImageInfo(ImageType imageType, int size) implements Serializable {
}
