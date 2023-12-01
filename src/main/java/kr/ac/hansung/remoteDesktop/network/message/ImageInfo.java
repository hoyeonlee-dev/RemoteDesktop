package kr.ac.hansung.remoteDesktop.network.message;

import java.io.Serializable;

public record ImageInfo(Type type, int size) implements Serializable {
    public enum Type {
        NO_UPDATE(0), //커서를 제외한 프레임이 이전 프레임과 차이가 없는 경우 : 업데이트 할 필요 없음
        UPDATE(1); // 커서를 제외한 프레임이 이전 프레임과 달라 클라이언트에게 새로운 프레임 전송
        private int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
