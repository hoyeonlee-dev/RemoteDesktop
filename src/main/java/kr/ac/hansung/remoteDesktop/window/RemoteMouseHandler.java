package kr.ac.hansung.remoteDesktop.window;

import java.awt.*;
import java.awt.event.KeyEvent;
import kr.ac.hansung.remoteDesktop.network.message.*;

public class RemoteMouseHandler {
    private Robot robot;
   
    // 화면 크기에 대한 변수
    private int screenWidth;
    private int screenHeight;
    
    // 좌표 변환 상수
    private static final double CONVERSION_FACTOR_X = 1.0; // X축 변환 비율
    private static final double CONVERSION_FACTOR_Y = 1.0; // Y축 변환 비율

    public RemoteMouseHandler() {
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void handleMouseMove(int x, int y) {
        // 클라이언트로부터 전송된 마우스 좌표 처리
        System.out.println("받은 마우스 이동 이벤트: X=" + x + ", Y=" + y);

        Point convertedPoint = convertCoordinates(x, y);

        // Robot 클래스를 사용하여 마우스를 이동 
        robot.mouseMove(convertedPoint.x, convertedPoint.y);
    }
    
    public void handleKeyEvent(KeyEventInfo keyEventInfo) {
        // 클라이언트로부터 전송된 키 이벤트 처리
        System.out.println("받은 키 이벤트: KeyCode=" + keyEventInfo.getKeyCode() + ", KeyPress=" + keyEventInfo.isKeyPress());

        int keyCode = keyEventInfo.getKeyCode();
        boolean keyPress = keyEventInfo.isKeyPress();

        if (keyPress) {
            robot.keyPress(keyCode);
        } else {
            robot.keyRelease(keyCode);
        }
    }

    private Point convertCoordinates(int x, int y) {
        int convertedX = (int) (x * CONVERSION_FACTOR_X);
        int convertedY = (int) (y * CONVERSION_FACTOR_Y);

        return new Point(convertedX, convertedY);
    }
}
