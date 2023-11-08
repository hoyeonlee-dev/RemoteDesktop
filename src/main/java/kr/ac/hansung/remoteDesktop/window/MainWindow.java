package kr.ac.hansung.remoteDesktop.window;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public static void main(String[] args) {
        var window = new MainWindow();
        var panel = new JPanel();
        window.add(panel);

        var serverButton = new JButton("서버");
        var clientButton = new JButton("클라이언트");

        var dimension = new Dimension();
        dimension.setSize(600, 400);
        window.setSize(dimension);

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

        window.setVisible(true);
    }
}
