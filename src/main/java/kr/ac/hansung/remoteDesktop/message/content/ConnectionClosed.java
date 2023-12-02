package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

public record ConnectionClosed(String message) implements Serializable {
}
