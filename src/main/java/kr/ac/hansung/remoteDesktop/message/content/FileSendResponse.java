package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

public class FileSendResponse implements Serializable {
    private Type type;

    public FileSendResponse(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

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

    @Override
    public String toString() {
        String sb = "FileSendResponse{" + "type=" + type +
                '}';
        return sb;
    }
}
