package kr.ac.hansung.remoteDesktop.network.message;

import java.io.Serializable;

public record FileMessage(String fileName, byte[] content) implements Serializable {
}
