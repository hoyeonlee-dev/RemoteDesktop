package kr.ac.hansung.remoteDesktop.window.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class AskPasswordDialog extends JDialog {
    JFrame         hostWindow;
    ActionListener submit;
    ActionListener cancel;
    JTextField textField;

    public AskPasswordDialog(JFrame hostWindow) {
        super(hostWindow, true);
        this.hostWindow = hostWindow;
    }

    public ActionListener getSubmit() {
        return submit;
    }

    public void setSubmit(ActionListener submit) {
        this.submit = submit;
    }

    public ActionListener getCancel() {
        return cancel;
    }

    public void setCancel(ActionListener cancel) {
        this.cancel = cancel;
    }

    public void setPosition(int x, int y) {
        var bounds = getBounds();
        setBounds(x, y, bounds.width, bounds.height);
    }

    public void setDialogSize(int width, int height) {
        var bounds = getBounds();
        setBounds(bounds.x, bounds.y, width, height);
    }

    private void configureDialog() {
        JPanel panel = new JPanel(new BorderLayout());

        textField = createTextField();
        panel.add(textField, BorderLayout.CENTER);
        panel.add(createButtonGroup(), BorderLayout.SOUTH);

        add(panel);
    }

    public String getPassword(){
        return textField.getText().trim();
    }
    private JPanel createButtonGroup() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        buttonPanel.add(createOkButton(), BorderLayout.CENTER);
        buttonPanel.add(createDenyButton(), BorderLayout.CENTER);

        return buttonPanel;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(30);
//        textField.setFocusable(false); // 포커스 불가능하게 설정
//        textField.setBackground(null); // 배경을 투명하게 설정
//        textField.setBorder(null); // 테두리 없애기
        return textField;
    }

    private JButton createOkButton() {
        var okButton = new JButton("전송");
        okButton.addActionListener(submit);
        return okButton;
    }

    private JButton createDenyButton() {
        var denyButton = new JButton("취소");
        denyButton.addActionListener(cancel);
        return denyButton;
    }

    public void start() {
        configureDialog();
        setVisible(true);
    }
}
