package kr.ac.hansung.remoteDesktop.util.screenCapture;

/**
 * 스크린 캡처를 구현하는 클래스들이 구현해야 하는 메서드를 담은 인터페이스
 *
 * @author hoyeon
 */
public interface IScreenCapture extends ICaptureResult {

    /**
     * 윈도우 창의 크기가 변경 되었을 때 호출 되는 메서드
     */
    void onWindowSizeUpdated();
    boolean doCapture();
}
