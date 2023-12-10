package kr.ac.hansung.remoteDesktop.message.content;

import java.io.Serializable;

// 메시지 타입만으로 충분한 의미전달을 했을 때 사용하는 메시지
public record Empty() implements Serializable {
}
