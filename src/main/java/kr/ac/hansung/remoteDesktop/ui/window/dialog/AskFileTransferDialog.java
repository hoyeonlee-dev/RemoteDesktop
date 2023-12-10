package kr.ac.hansung.remoteDesktop.ui.window.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 클라이언트로부터 파일 메시지를 받았을 때 나타나는 대화상자
 */
public class AskFileTransferDialog extends JDialog {
    JFrame hostWindow;
    ActionListener ok;
    ActionListener deny;
    List<String> fileNames;

    public AskFileTransferDialog(JFrame hostWindow) {
        super(hostWindow, true);
        this.hostWindow = hostWindow;
    }

    public ActionListener getOk() {
        return ok;
    }

    public void setOk(ActionListener ok) {
        this.ok = ok;
    }

    public ActionListener getDeny() {
        return deny;
    }

    public void setDeny(ActionListener deny) {
        this.deny = deny;
    }

    public void setPosition(int x, int y) {
        var bounds = getBounds();
        setBounds(x, y, bounds.width, bounds.height);
    }

    public void setDialogSize(int width, int height) {
        var bounds = getBounds();
        setBounds(bounds.x, bounds.y, width, height);
    }

    public void setDialogText(List<String> messages) {
        fileNames = messages;
    }

    private void configureDialog() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(createTextArea(), BorderLayout.CENTER);
        panel.add(createButtonGroup(), BorderLayout.SOUTH);

        add(panel);
    }

    private JPanel createButtonGroup() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        buttonPanel.add(createOkButton(), BorderLayout.CENTER);
        buttonPanel.add(createDenyButton(), BorderLayout.CENTER);

        return buttonPanel;
    }

    private JTextArea createTextArea() {

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false); // 편집 불가능하게 설정
        textArea.setFocusable(false); // 포커스 불가능하게 설정
        textArea.setBackground(null); // 배경을 투명하게 설정
        textArea.setBorder(null); // 테두리 없애기

        if (fileNames != null) {
            var sb = new StringBuilder();
            for (var fileName : fileNames) {
                sb.append(fileName).append('\n');
            }
            textArea.setText(sb.length() > 1 ? sb.toString() : "No File");
        }
        return textArea;
    }

    private JButton createOkButton() {
        var okButton = new JButton("수락");
        okButton.addActionListener(ok);
        return okButton;
    }

    private JButton createDenyButton() {
        var denyButton = new JButton("거절");
        denyButton.addActionListener(deny);
        return denyButton;
    }

    public void start() {
        configureDialog();
        setVisible(true);
    }
}
