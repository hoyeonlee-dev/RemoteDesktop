package kr.ac.hansung.remoteDesktop.window;

import java.io.Serializable;

public class RemoteMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private RemoteMessageType type;
    private Serializable data;

    public RemoteMessage(RemoteMessageType type, Serializable data) {
        this.type = type;
        this.data = data;
    }

    public RemoteMessageType getType() {
        return type;
    }

    public Serializable getData() {
        return data;
    }
}