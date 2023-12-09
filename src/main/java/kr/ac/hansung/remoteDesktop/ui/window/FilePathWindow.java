package kr.ac.hansung.remoteDesktop.ui.window;

import kr.ac.hansung.remoteDesktop.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FilePathWindow extends JFrame {
    private JTextField pathTextField;

    public FilePathWindow() {
        super("File Path");
        buildGUI();
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FilePathWindow();
            }
        });
    }

    private void buildGUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Select file save path:");
        panel.add(label);

        pathTextField = new JTextField();
        pathTextField.setEditable(false);
        panel.add(pathTextField);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings.getInstance().chooseSavePath();
                updatePathTextField();
            }
        });
        panel.add(browseButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panel.add(saveButton);

        add(panel);
        updatePathTextField();
    }

    private void updatePathTextField() {
        pathTextField.setText(Settings.getInstance().getSavePath());
    }
}
