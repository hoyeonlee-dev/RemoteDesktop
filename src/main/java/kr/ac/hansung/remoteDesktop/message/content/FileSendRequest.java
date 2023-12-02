package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;
import java.util.List;

public record FileSendRequest(String key, List<String> fileNames) implements Serializable {
}
