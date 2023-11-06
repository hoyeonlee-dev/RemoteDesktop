package kr.ac.hansung;

import java.io.Serializable;

public class Message implements Serializable {
    final private byte[] byteArray;

    public byte[] getByteArray() {
        return byteArray;
    }

    public Message(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public static void copyMessage(Message message, byte[] outBuffer) {
        System.arraycopy(message.byteArray, 0, outBuffer, 0, Integer.min(message.byteArray.length, outBuffer.length));
    }
}
