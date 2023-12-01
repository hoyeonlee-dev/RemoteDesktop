package kr.ac.hansung.remoteDesktop.window;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseListener;
import java.awt.event.*;

class HintTextField extends JTextField {
    private String hint;

    public HintTextField(String hint) {
        this.hint = hint;
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(hint)) {
                    setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(hint);
                }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty()) {
            int h = getHeight();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets ins = getInsets();
            FontMetrics fm = g.getFontMetrics();
            int c0 = getBackground().getRGB();
            int c1 = getForeground().getRGB();
            int m = 0xfefefefe;
            int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
            g.setColor(new Color(c2, true));
            g.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
        }
    }
}

public class MyWindow extends JFrame {
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
    private boolean clientVisible = true;
    private boolean hostVisible = false;
    
    private Socket serverSocket;
    
    private List<String> connectedComputers = new ArrayList<>();
    
    private JButton connectButton;
    private HintTextField t_search;
    
    private RemoteMouseSender remoteMouseSender;
    
    public MyWindow(int port) {
        super("window");
        buildGUI();
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = t_search.getText();
                String[] parts = input.split(":");
                
                if (parts.length != 2) {
                    JOptionPane.showMessageDialog(null, "Invalid input format. Please use 'ip:port'.");
                    return;
                }

                String serverAddress = parts[0].trim();
                int serverPort;

                try {
                    serverPort = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid port number.");
                    return;
                }

                try {
                    Socket serverSocket = new Socket(serverAddress, serverPort);
                    
                    remoteMouseSender = new RemoteMouseSender(serverSocket);
                    
                    openRemoteControlFrame(serverSocket);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to connect to the server.");
                    ex.printStackTrace();
                }
            }
        });

        // MyWindow 프레임에 마우스 이벤트 핸들러 등록
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // 마우스 좌표를 서버로 전송
            	try {
                    if (remoteMouseSender != null) {
                        remoteMouseSender.sendMouseMove(e.getX(), e.getY());
                    } else {
                        System.out.println("remoteMouseSender is null");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    remoteMouseSender.sendKeyEvent(e.getKeyCode(), true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    remoteMouseSender.sendKeyEvent(e.getKeyCode(), false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        MouseListener mouseClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int button = e.getButton();
                boolean pressed = true; 
                try {
                    remoteMouseSender.sendMouseClick(button, pressed);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        addMouseListener(mouseClickListener);
        addKeyListener(keyListener);

        setFocusable(true);
        requestFocusInWindow();
    }

    private void buildGUI() {
        cardPanel = new JPanel(new CardLayout());
        card2Panel = new JPanel(new CardLayout());

        computersPanel = createComputersPanel();
        computers2Panel = createComputers2Panel();
        settingsPanel = createSettingsPanel();
        settings2Panel = createClientPanel();
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

        p.setLayout(new BorderLayout());

        JLabel a = new JLabel("account ID");
        a.setForeground(Color.WHITE);
        a.setFont(new Font("SansSerif", Font.PLAIN, 20));

        a.setBorder(new EmptyBorder(0, 0, 0, 20));
        p.add(a, BorderLayout.EAST);

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
                String computerName = "컴퓨터1";  // 이 부분은 수정할 것임
                connectedComputers.add(computerName);
                updateConnectedComputersPanel();
            }
        });

        JButton reloadButton = new JButton("Reload");
        Font reloadButtonFont = new Font("SansSerif", Font.PLAIN, 15);
        reloadButton.setFont(reloadButtonFont);
        Dimension reloadButtonSize = new Dimension(90, 30);
        reloadButton.setPreferredSize(reloadButtonSize);
        reloadButton.setBackground(Color.DARK_GRAY);
        reloadButton.setForeground(Color.WHITE);

        buttonPanel.add(plusButton);
        buttonPanel.add(reloadButton);
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
	
	private JPanel createConnectPanel(String computerName) {
	    JPanel p = new JPanel() {
	        @Override
	        public Dimension getPreferredSize() {
	            return new Dimension(200, 550);
	        }
	    };
	    p.setBackground(Color.DARK_GRAY);
	    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

	    JButton connectButton = new JButton("연결하기");
	    Font connectButtonFont = new Font("SansSerif", Font.PLAIN, 15);
	    connectButton.setFont(connectButtonFont);
	    Dimension connectButtonSize = new Dimension(90, 30);
	    connectButton.setPreferredSize(connectButtonSize);
	    connectButton.setBackground(Color.DARK_GRAY);
	    connectButton.setForeground(Color.WHITE);

	    connectButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	String serverHostName = t_search.getText();

	            try {
	                InetAddress serverAddress = InetAddress.getByName(serverHostName);

	                // 서버 소켓 생성
	                serverSocket = new Socket(serverAddress, 12345);
	                
	                // 연결된 컴퓨터의 원격 제어 창 열기
	                openRemoteControlFrame(serverSocket);
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	    });

	    p.add(connectButton);

	    return p;
	}

	private void openRemoteControlFrame(Socket serverSocket) {
	    try {
	        // 서버에 소켓을 통해 접속
	        remoteMouseSender = new RemoteMouseSender(serverSocket);

	        // RemoteControlFrame을 생성하면서 remoteMouseSender 전달
	        RemoteControlFrame controlFrame = new RemoteControlFrame(remoteMouseSender);

	        // JFrame에 RemoteControlFrame을 추가
	        JFrame remoteControlFrame = new JFrame("원격 제어");
	        remoteControlFrame.add(controlFrame);

	        // 예시: 빈 창을 생성
	        remoteControlFrame.setSize(1920, 1080);
	        remoteControlFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        remoteControlFrame.setLocationRelativeTo(null);
	        remoteControlFrame.setVisible(true);
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error while opening remote control frame.");
	        e.printStackTrace();
	    }
	}



	private JPanel createComputers2Panel() {
	    JPanel p = new JPanel() {
	        @Override
	        public Dimension getPreferredSize() {
	            return new Dimension(200, 550);
	        }
	    };
	    p.setBackground(Color.DARK_GRAY);
	    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton cb = new JButton("Client");
        cb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clientVisible = true;
                hostVisible = false;

                if (clientPanel.getParent() == null) {
                    card2Panel.add(clientPanel, "clientPanel");
                }
                if (hostPanel.getParent() != null) {
                    card2Panel.remove(hostPanel);
                }

                CardLayout card2Layout = (CardLayout) card2Panel.getLayout();
                card2Layout.show(card2Panel, "clientPanel");

                card2Panel.revalidate();
                card2Panel.repaint();
            }
        });
        Font cbFont = new Font("SansSerif", Font.PLAIN, 15);
        cb.setFont(cbFont);
        Dimension cbSize = new Dimension(90, 30);
        cb.setPreferredSize(cbSize);
        cb.setBackground(Color.DARK_GRAY);
        cb.setForeground(Color.WHITE);

        JButton hb = new JButton("Host");
        hb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clientVisible = false;
                hostVisible = true;

                if (hostPanel.getParent() == null) {
                    card2Panel.add(hostPanel, "hostPanel");
                }
                if (clientPanel.getParent() != null) {
                    card2Panel.remove(clientPanel);
                }

                CardLayout card2Layout = (CardLayout) card2Panel.getLayout();
                card2Layout.show(card2Panel, "hostPanel");

                card2Panel.revalidate();
                card2Panel.repaint();
            }
        });
        Font hbFont = new Font("SansSerif", Font.PLAIN, 15);
        hb.setFont(hbFont);
        Dimension hbSize = new Dimension(70, 30);
        hb.setPreferredSize(hbSize);
        hb.setBackground(Color.DARK_GRAY);
        hb.setForeground(Color.WHITE);

        clientPanel = createClientPanel();
        hostPanel = createHostPanel();

        buttonPanel.add(cb);
        buttonPanel.add(hb);

        p.add(buttonPanel);

        return p;
    }

    private JPanel createClientPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.DARK_GRAY);
        JLabel clientLabel = new JLabel("CLIENT SETTINGS");
        clientLabel.setForeground(Color.WHITE);
        clientLabel.setFont(new Font("SansSerif", Font.PLAIN, 25));
        p.add(clientLabel);
        return p;
    }
    
    private JPanel createHostPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.DARK_GRAY);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel h = new JLabel("HOST SETTINGS");
        h.setForeground(Color.WHITE);
        h.setFont(new Font("SansSerif", Font.PLAIN, 25));
        p.add(h);

        p.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel hostingEnabled = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hostingEnabled.setBackground(Color.DARK_GRAY);

        JLabel e = new JLabel("Hosting Enabled ");
        e.setForeground(Color.WHITE);
        e.setFont(new Font("SansSerif", Font.PLAIN, 20));
        hostingEnabled.add(e);

        String[] comboBoxItems = {"Disabled", "Enabled"};
        JComboBox<String> comboBox = new JComboBox<>(comboBoxItems);
        comboBox.setBackground(Color.DARK_GRAY);
        comboBox.setForeground(Color.WHITE);
        hostingEnabled.add(comboBox);

        p.add(hostingEnabled);

        return p;
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
                int choice = JOptionPane.showOptionDialog(null, "Do you want to exit?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
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
    


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MyWindow(0);
            }
        });
    }
} 