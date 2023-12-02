package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

public record FileSendResponse(Type type) implements Serializable {
    public enum Type {
        OK(1), DENIED(0);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
