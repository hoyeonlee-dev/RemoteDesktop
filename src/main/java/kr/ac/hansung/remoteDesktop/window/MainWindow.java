package kr.ac.hansung.remoteDesktop.window;

import javax.swing.*;
import java.awt.*;

public class MainWindow implements IRDPWindow {
    JFrame mainWindow = null;

    public MainWindow() {
        this.mainWindow = new JFrame();
        initFrame();
    }

    public static void main(String[] args) {
        var window = new MainWindow();

        var panel = new JPanel();
        var serverButton = new JButton("서버");
        var clientButton = new JButton("클라이언트");
        panel.setLayout(new GridLayout(2, 1));
        panel.add(serverButton);
        panel.add(clientButton);

        serverButton.addActionListener(e ->
        {
            System.out.println("원격 호스트를 시작합니다");
            var host = new Thread(new RemoteHostWindow("원격 호스트"));
            host.start();
        });
        clientButton.addActionListener(e -> {
            System.out.println("원격 클라이언트를 시작합니다");
            var client = new Thread(new RemoteClientWindow("원격 클라이언트"));
            client.start();
        });

        window.add(panel);
        window.showClient();
    }

    private void initFrame() {
        if (mainWindow == null) throw new RuntimeException("메인 윈도우가 생성되지 않았습니다.");
        var dimension = new Dimension();
        dimension.setSize(600, 400);
        mainWindow.setSize(dimension);
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    public void showClient() {
        mainWindow.setVisible(true);
    }

    @Override
    public void hideClient() {
        mainWindow.setVisible(false);
    }

    @Override
    public void stopWindowAndService() {

    }

    @Override
    public void add(Component component) {
        mainWindow.add(component);
    }

    @Override
    public void add(Component component, Object constraints) {
        mainWindow.add(component, constraints);
    }
}
