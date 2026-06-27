package celestialsons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class GameClient {
    private static final int[] RADAR_RANGES_KM = {1, 10, 50, 100, 200, 1000};

    private final GameServerConnection server;
    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private final RadarCanvas radarCanvas;
    private final JLabel serverLabel;
    private final JLabel playerLabel;
    private final JLabel locationLabel;
    private final JLabel rangeLabel;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel loginMessage;

    private PlayerCharacter activePlayer;
    private int selectedRadarRangeKm = 100;

    public GameClient(GameServerConnection server) {
        this.server = server;
        this.frame = new JFrame("Celestial Sons");
        this.cardLayout = new CardLayout();
        this.rootPanel = new JPanel(this.cardLayout);
        this.radarCanvas = new RadarCanvas();
        this.serverLabel = new JLabel(" ");
        this.playerLabel = new JLabel(" ");
        this.locationLabel = new JLabel(" ");
        this.rangeLabel = new JLabel(" ");
        this.usernameField = new JTextField(20);
        this.passwordField = new JPasswordField(20);
        this.loginMessage = new JLabel(" ");

        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.frame.setSize(1200, 900);
        this.frame.setLocationRelativeTo(null);
        this.frame.setContentPane(this.rootPanel);

        this.rootPanel.add(buildLoginPanel(), "login");
        this.rootPanel.add(buildGamePanel(), "game");
        this.cardLayout.show(this.rootPanel, "login");
    }

    public void show() {
        this.frame.setVisible(true);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(11, 17, 28));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(19, 27, 42));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(61, 78, 107), 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        JLabel title = new JLabel("Celestial Sons");
        title.setForeground(new Color(235, 238, 244));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        form.add(title, c);

        c.gridy++;
        JLabel subtitle = new JLabel("Login");
        subtitle.setForeground(new Color(172, 184, 204));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 15f));
        form.add(subtitle, c);

        c.gridy++;
        this.serverLabel.setText("Server: " + this.server.getServerName());
        this.serverLabel.setForeground(new Color(172, 184, 204));
        form.add(this.serverLabel, c);

        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(Color.WHITE);
        form.add(usernameLabel, c);

        c.gridx = 1;
        this.usernameField.setColumns(18);
        form.add(this.usernameField, c);

        c.gridy++;
        c.gridx = 0;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Color.WHITE);
        form.add(passwordLabel, c);

        c.gridx = 1;
        this.passwordField.setColumns(18);
        form.add(this.passwordField, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        this.loginMessage.setForeground(new Color(246, 203, 88));
        form.add(this.loginMessage, c);

        JButton loginButton = new JButton("Log In");
        loginButton.addActionListener(this::handleLogin);

        c.gridy++;
        form.add(loginButton, c);

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        };
        this.usernameField.addKeyListener(enterKey);
        this.passwordField.addKeyListener(enterKey);

        panel.add(form);
        return panel;
    }

    private JPanel buildGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(9, 13, 22));

        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setBackground(new Color(12, 18, 29));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Radar");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        header.add(title);

        this.playerLabel.setForeground(new Color(215, 223, 235));
        header.add(this.playerLabel);

        this.locationLabel.setForeground(new Color(172, 184, 204));
        header.add(this.locationLabel);

        panel.add(header, BorderLayout.PAGE_START);

        this.radarCanvas.setPreferredSize(new Dimension(900, 700));
        panel.add(this.radarCanvas, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setBackground(new Color(12, 18, 29));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JPanel rangeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        rangeRow.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        Arrays.stream(RADAR_RANGES_KM).forEach(range -> {
            JToggleButton button = new JToggleButton(range + " km");
            button.setFocusPainted(false);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(28, 38, 58));
            button.setBorder(BorderFactory.createLineBorder(new Color(61, 78, 107), 1));
            button.addActionListener(e -> selectRadarRange(range));
            if (range == this.selectedRadarRangeKm) {
                button.setSelected(true);
            }
            group.add(button);
            rangeRow.add(button);
        });

        this.rangeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.rangeLabel.setForeground(new Color(172, 184, 204));

        footer.add(this.rangeLabel);
        footer.add(Box.createVerticalStrut(8));
        footer.add(rangeRow);

        panel.add(footer, BorderLayout.PAGE_END);
        return panel;
    }

    private void handleLogin(ActionEvent event) {
        String username = this.usernameField.getText().trim();
        char[] password = this.passwordField.getPassword();

        LoginResult result = this.server.loginOrCreateUser(username, password);
        Arrays.fill(password, '\0');
        this.passwordField.setText("");

        if (!result.isSuccess()) {
            this.loginMessage.setText(result.getMessage());
            return;
        }

        this.activePlayer = result.getPlayerCharacter();
        this.playerLabel.setText("Pilot: " + this.activePlayer.getName());
        this.locationLabel.setText("Location: " + this.activePlayer.getCurrentLocation().toLabel());
        this.rangeLabel.setText("Radar distance: " + this.selectedRadarRangeKm + " km");
        this.radarCanvas.repaint();
        this.loginMessage.setText(result.getMessage());
        this.cardLayout.show(this.rootPanel, "game");
    }

    private void selectRadarRange(int rangeKm) {
        this.selectedRadarRangeKm = rangeKm;
        this.rangeLabel.setText("Radar distance: " + rangeKm + " km");
        this.radarCanvas.repaint();
    }

    private class RadarCanvas extends JPanel {
        RadarCanvas() {
            setBackground(new Color(6, 10, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 2 - 90;
            if (radius < 80) {
                radius = Math.max(60, Math.min(width, height) / 2 - 20);
            }

            g2.setColor(new Color(8, 13, 20));
            g2.fillRect(0, 0, width, height);

            g2.setColor(new Color(36, 54, 72));
            g2.drawLine(centerX - radius, centerY, centerX + radius, centerY);
            g2.drawLine(centerX, centerY - radius, centerX, centerY + radius);

            g2.setColor(new Color(59, 82, 105));
            for (int i = 1; i <= 5; i++) {
                int ringRadius = (int) Math.round(radius * (i / 5.0));
                g2.drawOval(centerX - ringRadius, centerY - ringRadius, ringRadius * 2, ringRadius * 2);
            }

            g2.setColor(new Color(115, 131, 153));
            g2.drawString("0 km", centerX + 8, centerY - 6);
            int[] ringValues = rangeValues();
            for (int i = 0; i < ringValues.length; i++) {
                int ringRadius = (int) Math.round(radius * ((i + 1) / 5.0));
                g2.drawString(ringValues[i] + " km", centerX + ringRadius + 6, centerY - 6);
            }

            Polygon shipTriangle = new Polygon();
            shipTriangle.addPoint(centerX, centerY - 10);
            shipTriangle.addPoint(centerX - 8, centerY + 9);
            shipTriangle.addPoint(centerX + 8, centerY + 9);
            g2.setColor(new Color(255, 226, 95));
            g2.fillPolygon(shipTriangle);
            g2.setColor(new Color(255, 245, 171));
            g2.drawPolygon(shipTriangle);
            g2.fillOval(centerX - 2, centerY - 2, 4, 4);

            g2.dispose();
        }

        private int[] rangeValues() {
            int maxRange = selectedRadarRangeKm;
            return new int[] {
                    Math.max(1, maxRange / 5),
                    Math.max(1, (maxRange * 2) / 5),
                    Math.max(1, (maxRange * 3) / 5),
                    Math.max(1, (maxRange * 4) / 5),
                    maxRange
            };
        }
    }
}
