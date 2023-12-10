package kr.ac.hansung.remoteDesktop.ui.window;

import kr.ac.hansung.remoteDesktop.Settings;
import kr.ac.hansung.remoteDesktop.client.sender.RemoteMouseSender;
import kr.ac.hansung.remoteDesktop.ui.component.HintTextField;
import kr.ac.hansung.remoteDesktop.ui.window.example.RemoteControlFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainWindow extends JFrame {
    private RemoteHostDaemon remoteHostDaemon;
    private final List<String> connectedComputers = new ArrayList<>();
    private JPanel cardPanel;
    private JPanel card2Panel;
    private JPanel accountPanel;
    private JPanel computersPanel;
    private JPanel computers2Panel;
    private JPanel settingsPanel;
    private JPanel settings2Panel;
    private JPanel clientPanel;
    private JPanel hostPanel;
    private boolean computersVisible = true;
    private boolean settingsVisible = false;
    private final boolean clientVisible = true;
    private final boolean hostVisible = false;
    private HintTextField t_search;
    private JLabel ipa;
    
    private JTextField pathTextField;

    private RemoteMouseSender remoteMouseSender;

    public MainWindow() {
        super("원격 데스크톱");
        if (Settings.getInstance().isAllowHosting()) {
            remoteHostDaemon = new RemoteHostDaemon();
            remoteHostDaemon.setParentWindow(this);
            remoteHostDaemon.start();
        } else {
            remoteHostDaemon = null;
        }

        buildGUI();
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow();
            }
        });
    }

    private void buildGUI() {
        cardPanel = new JPanel(new CardLayout());
        card2Panel = new JPanel(new CardLayout());

        computersPanel = createComputersPanel();
        computers2Panel = createComputers2Panel();
        settingsPanel = createSettingsPanel();
        settings2Panel = createHostPanel();
        accountPanel = accountPanel();

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(accountPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(card2Panel, BorderLayout.SOUTH);

        cardPanel.add(computersPanel, "computers");
        card2Panel.add(computers2Panel, "computers2");
        cardPanel.add(settingsPanel, "settings");
        card2Panel.add(settings2Panel, "settings2");

        add(mainPanel);
        createControlPanel();
    }

    private JPanel accountPanel() {
        JPanel p = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 60);
            }
        };

        p.setBackground(Color.DARK_GRAY);
        return p;
    }

    private JPanel createComputersPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.DARK_GRAY);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel c = new JLabel("Computers");
        c.setForeground(Color.WHITE);
        c.setFont(new Font("SansSerif", Font.PLAIN, 50));
        p.add(c);

        JLabel d = new JLabel("Connect to your computer or a friend's computer in low latency desktop mode.");
        d.setForeground(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.PLAIN, 20));
        p.add(d);

        JPanel s = new JPanel();
        s.setBackground(Color.DARK_GRAY);
        s.setLayout(new FlowLayout());

        this.t_search = new HintTextField("Search Hosts and Computers");
        this.t_search.setColumns(40);
        this.t_search.setFont(new Font("SansSerif", Font.PLAIN, 15));
        s.add(this.t_search);

        s.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton plusButton = new JButton("+");
        Font buttonFont = new Font("SansSerif", Font.PLAIN, 20);
        plusButton.setFont(buttonFont);
        Dimension buttonSize = new Dimension(50, 30);
        plusButton.setPreferredSize(buttonSize);
        plusButton.setBackground(Color.DARK_GRAY);
        plusButton.setForeground(Color.WHITE);

        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String computerName = t_search.getText().trim();
                connectedComputers.add(computerName);
                updateConnectedComputersPanel();
                
//                ipa.setText(computerName);
            }
        });

        buttonPanel.add(plusButton);
        s.add(buttonPanel);

        p.add(s);

        return p;
    }

    private void updateConnectedComputersPanel() {
        card2Panel.remove(computers2Panel);
        card2Panel.add(createComputers2Panel(), "computers2");
        
        CardLayout card2Layout = (CardLayout) card2Panel.getLayout();
        card2Layout.show(card2Panel, "computers2");
        card2Panel.revalidate();
        card2Panel.repaint();
    }
       
    private void updateConnectedComputersPanel(String computerName) {
        JPanel connectPanel = createConnectPanel(computerName);
        card2Panel.add(connectPanel, "computers2");
        
        connectedComputers.add(computerName);

        CardLayout card2Layout = (CardLayout) card2Panel.getLayout();
        card2Layout.show(card2Panel, "computers2");

        card2Panel.revalidate();
        card2Panel.repaint();
    }

    private JPanel createConnectPanel(String computerName) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel computerIconLabel = new JLabel(
                resizeImageIcon(new ImageIcon(getClass().getClassLoader().getResource("computer_icon.png")), 80, 80));
        computerIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(computerIconLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);

        JLabel ipa = new JLabel(computerName);
        ipa.setForeground(Color.WHITE);
        ipa.setFont(new Font("SansSerif", Font.PLAIN, 15));  

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.anchor = GridBagConstraints.CENTER;
        p.add(ipa, gbc2);

        JButton connectButton = new JButton("연결하기");
        Font connectButtonFont = new Font("SansSerif", Font.PLAIN, 15);
        connectButton.setFont(connectButtonFont);
        Dimension connectButtonSize = new Dimension(120, 30);
        connectButton.setPreferredSize(connectButtonSize);
        connectButton.setBackground(Color.DARK_GRAY);
        connectButton.setForeground(Color.WHITE);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 0;
        gbc3.gridy = 2;
        gbc3.anchor = GridBagConstraints.CENTER;
        gbc3.insets = new Insets(10, 0, 0, 0);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String address = ipa.getText().trim();
                new Thread(new RemoteClientWindow(String.format("%s에 연결 중", address), address)).start();
            }
        });

        p.add(connectButton, gbc3);

        return p;
    }


    private void openRemoteControlFrame(Socket serverSocket) {
        RemoteControlFrame controlFrame = new RemoteControlFrame(remoteMouseSender);

        JFrame remoteControlFrame = new JFrame("원격 제어");
        remoteControlFrame.add(controlFrame);

        remoteControlFrame.setSize(1920, 1080);
        remoteControlFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        remoteControlFrame.setLocationRelativeTo(null);
        remoteControlFrame.setVisible(true);
        JOptionPane.showMessageDialog(null, "Error while opening remote control frame.");
    }

    private JPanel createComputers2Panel() {
        JPanel p = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 550);
            }
        };
        p.setBackground(Color.DARK_GRAY);
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 100, 15));

        if (connectedComputers.isEmpty()) {
            JPanel cp = new JPanel();
            cp.setBackground(Color.DARK_GRAY);
            JLabel n = new JLabel("You have no computers available right now.");
            n.setFont(new Font("SansSerif", Font.PLAIN, 20));
            n.setForeground(Color.WHITE);
            n.setHorizontalAlignment(JLabel.CENTER);
            cp.add(n);
            p.add(cp);
        } else {
            for (String computerName : connectedComputers) {
                p.add(createConnectPanel(computerName));
            }
        }

        return p;
    }

    private JPanel createSettingsPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.DARK_GRAY);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel c = new JLabel("Settings");
        c.setForeground(Color.WHITE);
        c.setFont(new Font("SansSerif", Font.PLAIN, 50));
        p.add(c);

        JLabel d = new JLabel("Customize your experience.");
        d.setForeground(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.PLAIN, 20));
        p.add(d);

        p.add(Box.createRigidArea(new Dimension(0, 30)));
        
        JPanel setPanel = new JPanel();
        setPanel.setBackground(Color.DARK_GRAY);
        
        JLabel h = new JLabel("HOST SETTINGS");
        h.setForeground(Color.WHITE);
        h.setFont(new Font("SansSerif", Font.PLAIN, 25));
        setPanel.add(h);

        hostPanel = createHostPanel();

        p.add(setPanel);

        return p;
    }

    public void settingsUpdated() {

    }

    private JPanel createHostPanel() {
    	JPanel p = new JPanel();
        p.setBackground(Color.DARK_GRAY);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Password
        JPanel password = new JPanel();
        password.setBackground(Color.DARK_GRAY);

        JLabel pw = new JLabel("               Password   ");
        pw.setForeground(Color.WHITE);
        pw.setFont(new Font("SansSerif", Font.PLAIN, 20));
        password.add(pw);
        
        password.add(Box.createRigidArea(new Dimension(15, 0)));

        JTextField passwordTextField = new JTextField(20);
        passwordTextField.setBackground(Color.DARK_GRAY);
        passwordTextField.setForeground(Color.WHITE);
        passwordTextField.setText(Settings.getInstance().getPassword());
        password.add(passwordTextField);

        p.add(password);
        
        //save file to
        JPanel sf = new JPanel();
        sf.setBackground(Color.DARK_GRAY);

        JLabel s = new JLabel("                              Save File To   ");
        s.setForeground(Color.WHITE);
        s.setFont(new Font("SansSerif", Font.PLAIN, 20));
        sf.add(s);
        
        pathTextField = new JTextField();
        pathTextField.setColumns(20);
        pathTextField.setBackground(Color.DARK_GRAY);
        pathTextField.setForeground(Color.WHITE);
        pathTextField.setEditable(false);
        sf.add(pathTextField);
        
        JButton browseButton = new JButton("Browse");
        browseButton.setBackground(Color.DARK_GRAY);
        browseButton.setForeground(Color.WHITE);
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings.getInstance().chooseSavePath();
                updatePathTextField(); //파일 저장 경로 설정 버튼에 대한 이벤트 핸들러
            }
        });
        sf.add(browseButton);
        
        p.add(sf);
        
        // Enable Hosting, Enable Client ON/OFF
        JPanel eh = new JPanel();
        eh.setBackground(Color.DARK_GRAY);

        JLabel e = new JLabel("Enable Hosting   ");
        e.setForeground(Color.WHITE);
        e.setFont(new Font("SansSerif", Font.PLAIN, 20));
        eh.add(e);
        
        eh.add(Box.createRigidArea(new Dimension(25, 0)));

        String[] comboBoxItems = {"ON",
                                  "OFF"
        };
        JComboBox<String> comboBox = new JComboBox<>(comboBoxItems);
        comboBox.setBackground(Color.DARK_GRAY);
        comboBox.setForeground(Color.WHITE);
        eh.add(comboBox);

        MainWindow t = this;
        
        JPanel ec = new JPanel();
        ec.setBackground(Color.DARK_GRAY);

        JLabel ecLabel = new JLabel("Enable Client Input   ");
        ecLabel.setForeground(Color.WHITE);
        ecLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        ec.add(ecLabel);

        JComboBox<String> comboBox2 = new JComboBox<>(comboBoxItems);
        comboBox2.setBackground(Color.DARK_GRAY);
        comboBox2.setForeground(Color.WHITE);
        ec.add(comboBox2);

        // update Settings.password
        passwordTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                Settings.getInstance().setPassword(passwordTextField.getText().trim());
            }
        });

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String item = (String) e.getItem();
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                if (item.equals("ON") && selected) {
                    remoteHostDaemon = new RemoteHostDaemon();
                    remoteHostDaemon.setParentWindow(t);
                    remoteHostDaemon.start();
                } else if (item.equals("OFF") && selected) {
                    if (remoteHostDaemon != null) {
                        remoteHostDaemon.stop();
                    }
                }
            }
        });

        comboBox2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String item = (String) e.getItem();
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                if (item.equals("ON") && selected) {
                    Settings.getInstance().setAllowClientInput(true);
                } else if (item.equals("OFF") && selected) {
                    Settings.getInstance().setAllowClientInput(false);
                }
            }
        });
        
        p.add(eh);
        p.add(ec);
        
        JPanel p2 = new JPanel();
        p2.setBackground(Color.DARK_GRAY);
        
        JPanel leftAlignedPanel = new JPanel(new BorderLayout());
        leftAlignedPanel.add(p, BorderLayout.WEST);
        leftAlignedPanel.add(p2, BorderLayout.CENTER);
        
        return leftAlignedPanel;
    }
    
    private void updatePathTextField() { //UI에서 파일 저장 경로를 업데이트하는 메서드
        pathTextField.setText(Settings.getInstance().getSavePath());
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(Color.BLACK);

        ImageIcon computersIcon = new ImageIcon(getClass().getClassLoader().getResource("computers.jpg"));
        ImageIcon settingsIcon = new ImageIcon(getClass().getClassLoader().getResource("settings.jpg"));
        ImageIcon exitIcon = new ImageIcon(getClass().getClassLoader().getResource("logout.jpg"));

        ImageIcon resizedComputersIcon = resizeImageIcon(computersIcon, 64, 64);
        ImageIcon resizedSettingsIcon = resizeImageIcon(settingsIcon, 64, 64);
        ImageIcon resizedExitIcon = resizeImageIcon(exitIcon, 64, 64);

        JLabel computersLabel = new JLabel(resizedComputersIcon);
        JLabel settingsLabel = new JLabel(resizedSettingsIcon);
        JLabel exitLabel = new JLabel(resizedExitIcon);

        computersLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                computersVisible = true;
                settingsVisible = false;

                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                CardLayout card2Layout = (CardLayout) card2Panel.getLayout();
                cardLayout.show(cardPanel, computersVisible ? "computers" : "settings");
                card2Layout.show(card2Panel, computersVisible ? "computers2" : "settings2");

            }
        });

        settingsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                computersVisible = false;
                settingsVisible = true;

                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                CardLayout card2Layout = (CardLayout) card2Panel.getLayout();
                cardLayout.show(cardPanel, computersVisible ? "computers" : "settings");
                card2Layout.show(card2Panel, computersVisible ? "computers2" : "settings2");

            }
        });

        exitLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int choice = JOptionPane.showOptionDialog(null, "Do you want to exit?", "Confirmation",
                                                          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                          null, null);
                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        controlPanel.add(Box.createVerticalGlue());
        controlPanel.add(computersLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(settingsLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(exitLabel);
        controlPanel.add(Box.createVerticalGlue());

        add(controlPanel, BorderLayout.WEST);
    }

    private ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
        Image image = icon.getImage();
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}  