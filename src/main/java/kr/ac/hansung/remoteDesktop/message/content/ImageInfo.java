package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 영상 소켓을 통해 동적인 크기를 갖는 이미지를 전송하기에 앞서 보내는 메시지
public class ImageInfo implements Serializable {
    private Type type;
    private int size;

    public ImageInfo(Type type, int size) {
        this.type = type;
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public enum Type {
        NO_UPDATE(0), //커서를 제외한 프레임이 이전 프레임과 차이가 없는 경우 : 업데이트 할 필요 없음
        UPDATE(1); // 커서를 제외한 프레임이 이전 프레임과 달라 클라이언트에게 새로운 프레임 전송
        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    @Override
    public String toString() {
        String sb = "ImageInfo{" + "type=" + type +
                ", size=" + size +
                '}';
        return sb;
    }
}
