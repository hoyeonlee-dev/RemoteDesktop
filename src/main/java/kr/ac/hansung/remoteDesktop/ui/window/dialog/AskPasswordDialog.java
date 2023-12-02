package kr.ac.hansung.remoteDesktop.ui.window.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AskPasswordDialog extends JDialog {
    JFrame         hostWindow;
    ActionListener submit;
    ActionListener cancel;
    private final String QUESTION_WHEN_FIRST_TIME     = "암호를 입력하세요";
    private final String QUESTION_WHEN_WRONG_PASSWORD = "틀렸습니다. 다시 입력하세요";
    JTextField textField;
    Type       type;

    public AskPasswordDialog(Type type, JFrame hostWindow) {
        super(hostWindow, true);
        this.hostWindow = hostWindow;
        this.type       = type;
    }

    private void configureDialog() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(createInputPanel(), BorderLayout.CENTER);
        panel.add(createButtonGroup(), BorderLayout.SOUTH);

        add(panel);
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

    public String getPassword() {
        return textField.getText().trim();
    }

    private JPanel createInputPanel() {
        var panel = new JPanel(new GridLayout(2, 1));
        var label = new JLabel();
        label.setText(type == Type.FIRST ? QUESTION_WHEN_FIRST_TIME : QUESTION_WHEN_WRONG_PASSWORD);
        textField = new JTextField(30);
        panel.add(label);
        panel.add(textField);
        return panel;
    }

    private JPanel createButtonGroup() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        buttonPanel.add(createOkButton(), BorderLayout.CENTER);
        buttonPanel.add(createDenyButton(), BorderLayout.CENTER);

        return buttonPanel;
    }

    public enum Type {
        FIRST(0),
        WRONG(1);
        int value;

        Type(int value) {
            this.value = value;
        }
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