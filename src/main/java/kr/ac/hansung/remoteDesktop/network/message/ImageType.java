package kr.ac.hansung.remoteDesktop.network.message;

public enum ImageType {
    NO_UPDATE(0), UPDATE(1);
    private int value;

    ImageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
