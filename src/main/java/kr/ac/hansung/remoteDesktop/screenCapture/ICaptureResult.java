package kr.ac.hansung.remoteDesktop.screenCapture;

import java.awt.image.BufferedImage;

/**
 * 비트맵을 네트워크을 통해 전송하고자 하면 구현해야 하는 메서드를 담은 인터페이스
 *
 * @author hoyeon
 */
public interface ICaptureResult {
    /**
     * 캡처한 이미지를 네트워크를 통해 전송하기 위해 구현해야 하는 메서드
     *
     * @return 픽셀을 담은 배열
     */
    byte[] getFrameBuffer();

    /**
     * 스크린 캡처 결과를 사용하여 이미지 생성
     *
     * @return 스크린 샷을 담은 이미지
     */
    BufferedImage createBufferedImage();
}
