package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

public record FileMessage(String fileName, byte[] content) implements Serializable {
}
