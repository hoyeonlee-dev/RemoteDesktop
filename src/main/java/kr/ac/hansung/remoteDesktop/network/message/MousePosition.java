package kr.ac.hansung.remoteDesktop.network.message;

import java.io.Serializable;

public record MousePosition(int x, int y) implements Serializable {
}
