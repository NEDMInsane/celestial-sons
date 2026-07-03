package celestialsons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.List;

import celestialsons.orbitalbodies.Star;

import static java.lang.Math.clamp;

public class GameClient {
    private static final int[] RADAR_RANGES_KM = {1, 10, 50, 100, 200, 1000};
    private static final double SYSTEM_MAP_SCALE = 1.0;
    private static final int SYSTEM_MAP_PAN_STEP = 120;

    private final GameServerConnection server;
    private final JFrame frame;
    private final CardLayout rootCardLayout;
    private final JPanel rootPanel;
    private final CardLayout viewCardLayout;
    private final JPanel viewPanel;
    private final CardLayout controlsCardLayout;
    private final JPanel controlsPanel;
    private final RadarPanel radarPanel;
    private final SystemMapPanel systemMapPanel;
    private final StarMapPanel starMapPanel;
    private final StationPanel stationPanel;
    private final JLabel serverLabel;
    private final JLabel playerLabel;
    private final JLabel locationLabel;
    private final JLabel statusLabel;
    private final JLabel radarRangeLabel;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel loginMessage;
    private final JPanel radarRangeRow;
    private final JToggleButton radarTabButton;
    private final JToggleButton systemMapTabButton;
    private final JPanel spaceControlsPanel;
    private final JPanel dockedControlsPanel;
    private final JButton undockButton;
    private final JButton marketButton;
    private final JButton shipyardButton;
    private final JButton characterButton;
    private final JTextArea systemDetailsArea;
    private final JTextArea characterDetailsArea;
    private final JTextArea flightPlanDetailsArea;

    private PlayerCharacter activePlayer;
    private String loggedInUsername;
    private SystemMapState systemMapState;
    private StationState stationState;
    private String activeView = "login";
    private int selectedRadarRangeKm = 100;
    private double mapPanX = 0.0;
    private double mapPanY = 0.0;
    private double systemMapZoom = 1.0;
    private double starMapZoom = 1.0;
    private Point systemMapMousePoint = null;
    private String hoveredOrbitalLabel = null;
    private final Timer stateRefreshTimer;

    public GameClient(GameServerConnection server) {
        this.server = server;
        this.frame = new JFrame("Celestial Sons");
        this.rootCardLayout = new CardLayout();
        this.rootPanel = new JPanel(this.rootCardLayout);
        this.viewCardLayout = new CardLayout();
        this.viewPanel = new JPanel(this.viewCardLayout);
        this.controlsCardLayout = new CardLayout();
        this.controlsPanel = new JPanel(this.controlsCardLayout);
        this.radarPanel = new RadarPanel();
        this.systemMapPanel = new SystemMapPanel();
        this.starMapPanel = new StarMapPanel();
        this.serverLabel = new JLabel(" ");
        this.playerLabel = new JLabel(" ");
        this.locationLabel = new JLabel(" ");
        this.statusLabel = new JLabel(" ");
        this.radarRangeLabel = new JLabel(" ");
        this.usernameField = new JTextField(20);
        this.passwordField = new JPasswordField(20);
        this.loginMessage = new JLabel(" ");
        this.radarRangeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        this.radarTabButton = new JToggleButton("Radar Scope");
        this.systemMapTabButton = new JToggleButton("System Map");
        this.spaceControlsPanel = new JPanel();
        this.dockedControlsPanel = new JPanel(new BorderLayout());
        this.undockButton = new JButton("Undock");
        this.marketButton = new JButton("Market");
        this.shipyardButton = new JButton("Shipyard");
        this.characterButton = new JButton("Character");
        this.systemDetailsArea = createReadOnlyArea();
        this.characterDetailsArea = createReadOnlyArea();
        this.flightPlanDetailsArea = createReadOnlyArea();
        this.stationPanel = new StationPanel();

        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.frame.setSize(1280, 920);
        this.frame.setLocationRelativeTo(null);
        this.frame.setContentPane(this.rootPanel);

        this.rootPanel.add(buildLoginPanel(), "login");
        this.rootPanel.add(buildGamePanel(), "game");
        this.rootCardLayout.show(this.rootPanel, "login");
        this.stateRefreshTimer = new Timer(250, e -> refreshActiveState());
        this.stateRefreshTimer.start();
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

        JPanel header = new JPanel(new GridLayout(4, 1));
        header.setBackground(new Color(12, 18, 29));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Celestial Sons");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        header.add(title);

        this.playerLabel.setForeground(new Color(215, 223, 235));
        header.add(this.playerLabel);

        this.locationLabel.setForeground(new Color(172, 184, 204));
        header.add(this.locationLabel);

        this.statusLabel.setForeground(new Color(172, 184, 204));
        header.add(this.statusLabel);

        panel.add(header, BorderLayout.PAGE_START);

        this.viewPanel.add(this.radarPanel, "radar");
        this.viewPanel.add(buildSystemMapPanel(), "systemMap");
        this.viewPanel.add(this.starMapPanel, "starMap");
        this.viewPanel.add(this.stationPanel, "station");
        panel.add(this.viewPanel, BorderLayout.CENTER);

        this.spaceControlsPanel.setBackground(new Color(12, 18, 29));
        this.spaceControlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        this.spaceControlsPanel.setLayout(new BoxLayout(this.spaceControlsPanel, BoxLayout.Y_AXIS));

        this.radarRangeRow.setOpaque(false);
        ButtonGroup radarRanges = new ButtonGroup();
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
            radarRanges.add(button);
            this.radarRangeRow.add(button);
        });

        JPanel tabRow = new JPanel();
        tabRow.setOpaque(false);
        tabRow.setLayout(new BoxLayout(tabRow, BoxLayout.X_AXIS));
        this.radarTabButton.setFocusPainted(false);
        this.systemMapTabButton.setFocusPainted(false);
        this.radarTabButton.setForeground(Color.WHITE);
        this.systemMapTabButton.setForeground(Color.WHITE);
        this.radarTabButton.setBackground(new Color(28, 38, 58));
        this.systemMapTabButton.setBackground(new Color(28, 38, 58));
        this.radarTabButton.addActionListener(e -> switchView("radar"));
        this.systemMapTabButton.addActionListener(e -> switchView("systemMap"));
        ButtonGroup tabs = new ButtonGroup();
        tabs.add(this.radarTabButton);
        tabs.add(this.systemMapTabButton);
        this.radarTabButton.setSelected(true);
        tabRow.add(this.radarTabButton);
        tabRow.add(Box.createHorizontalStrut(8));
        tabRow.add(this.systemMapTabButton);
        tabRow.add(Box.createHorizontalGlue());

        this.radarRangeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.radarRangeLabel.setForeground(new Color(172, 184, 204));

        this.spaceControlsPanel.add(this.radarRangeLabel);
        this.spaceControlsPanel.add(Box.createVerticalStrut(8));
        this.spaceControlsPanel.add(this.radarRangeRow);
        this.spaceControlsPanel.add(Box.createVerticalStrut(8));
        this.spaceControlsPanel.add(buildSpaceModeButtons());

        buildDockedControlsPanel();
        this.controlsPanel.add(this.spaceControlsPanel, "space");
        this.controlsPanel.add(this.dockedControlsPanel, "docked");
        panel.add(this.controlsPanel, BorderLayout.PAGE_END);
        updateBottomControls();
        return panel;
    }

    private JPanel buildSystemMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(9, 13, 22));

        this.systemMapPanel.setPreferredSize(new Dimension(840, 680));
        panel.add(this.systemMapPanel, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(320, 680));
        sidePanel.setBackground(new Color(10, 16, 26));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sidePanel.setLayout(new GridLayout(3, 1, 0, 10));

        sidePanel.add(buildDetailSection("System", this.systemDetailsArea));
        sidePanel.add(buildDetailSection("Characters", this.characterDetailsArea));
        sidePanel.add(buildDetailSection("Flight Plans", this.flightPlanDetailsArea));

        panel.add(sidePanel, BorderLayout.LINE_END);
        return panel;
    }

    private JPanel buildStationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(7, 10, 16));

        JPanel leftMenu = new JPanel();
        leftMenu.setPreferredSize(new Dimension(260, 680));
        leftMenu.setBackground(new Color(12, 16, 24));
        leftMenu.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        leftMenu.setLayout(new GridLayout(5, 1, 0, 12));

        this.characterButton.setFocusPainted(false);
        this.marketButton.setFocusPainted(false);
        this.shipyardButton.setFocusPainted(false);

        this.characterButton.setBackground(new Color(28, 38, 58));
        this.marketButton.setBackground(new Color(28, 38, 58));
        this.shipyardButton.setBackground(new Color(28, 38, 58));
        this.characterButton.setForeground(Color.WHITE);
        this.marketButton.setForeground(Color.WHITE);
        this.shipyardButton.setForeground(Color.WHITE);

        this.characterButton.addActionListener(e -> openCharacterSheet());
        this.marketButton.addActionListener(e -> openMarketMenu());
        this.shipyardButton.addActionListener(e -> openShipyardMenu());

        JLabel heading = new JLabel("Station Tasks");
        heading.setForeground(Color.WHITE);
        leftMenu.add(heading);
        leftMenu.add(this.characterButton);
        leftMenu.add(this.marketButton);
        leftMenu.add(this.shipyardButton);
        leftMenu.add(Box.createVerticalGlue());

        panel.add(leftMenu, BorderLayout.LINE_START);
        panel.add(new StationSilhouettePanel(), BorderLayout.CENTER);
        return panel;
    }

    private void buildDockedControlsPanel() {
        this.dockedControlsPanel.removeAll();
        JPanel dockBar = new JPanel(new BorderLayout());
        dockBar.setBackground(new Color(12, 18, 29));
        dockBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        this.undockButton.setBackground(new Color(140, 16, 16));
        this.undockButton.setForeground(Color.WHITE);
        this.undockButton.setFocusPainted(false);
        this.undockButton.addActionListener(e -> handleUndock());

        dockBar.add(new JLabel("Docked at station"), BorderLayout.LINE_START);
        dockBar.add(this.undockButton, BorderLayout.LINE_END);

        JPanel dockControls = new JPanel();
        dockControls.setBackground(new Color(12, 18, 29));
        dockControls.setLayout(new BoxLayout(dockControls, BoxLayout.Y_AXIS));
        dockControls.add(buildDockedModeButtons());
        dockControls.add(Box.createVerticalStrut(8));
        dockControls.add(dockBar);

        this.dockedControlsPanel.add(dockControls, BorderLayout.CENTER);
    }

    private JPanel buildSpaceModeButtons() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JButton radarButton = createModeButton("Radar Scope", this::selectRadarView);
        JButton systemButton = createModeButton("System Map", this::selectSystemMapView);
        JButton starButton = createModeButton("Star Map", this::selectStarMapView);

        row.add(radarButton);
        row.add(systemButton);
        row.add(starButton);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JPanel buildDockedModeButtons() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JButton stationButton = createModeButton("Station", this::selectStationView);
        JButton starButton = createModeButton("Star Map", this::selectStarMapView);

        row.add(stationButton);
        row.add(starButton);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JButton createModeButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(28, 38, 58));
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel buildDetailSection(String title, JTextArea area) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(new Color(16, 24, 37));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(49, 64, 88), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel label = new JLabel(title);
        label.setForeground(Color.WHITE);
        section.add(label, BorderLayout.PAGE_START);
        section.add(new JScrollPane(area), BorderLayout.CENTER);
        return section;
    }

    private JTextArea createReadOnlyArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(new Color(20, 28, 42));
        area.setForeground(new Color(215, 223, 235));
        area.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        return area;
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
        this.loggedInUsername = username;
        this.loginMessage.setText(result.getMessage());
        this.stationState = this.server.getStationState(this.loggedInUsername);

        if (this.stationState != null && !this.stationState.isCharacterCreated()) {
            openCharacterCreationDialog();
        } else if (this.stationState != null && this.stationState.isDocked()) {
            refreshDockedState();
            selectStationView();
            this.rootCardLayout.show(this.rootPanel, "game");
        } else {
            refreshSpaceState();
            selectRadarView();
            this.rootCardLayout.show(this.rootPanel, "game");
        }
    }

    private void selectRadarRange(int rangeKm) {
        this.selectedRadarRangeKm = rangeKm;
        this.radarRangeLabel.setText("Radar distance: " + rangeKm + " km");
        this.radarPanel.repaint();
    }

    private void switchView(String view) {
        if ("systemMap".equals(view)) {
            selectSystemMapView();
        } else if ("starMap".equals(view)) {
            selectStarMapView();
        } else if ("station".equals(view)) {
            selectStationView();
        } else {
            selectRadarView();
        }
    }

    private void selectRadarView() {
        this.activeView = "radar";
        this.radarTabButton.setSelected(true);
        this.viewCardLayout.show(this.viewPanel, "radar");
        updateBottomControls();
        this.radarPanel.repaint();
    }

    private void selectSystemMapView() {
        this.activeView = "systemMap";
        refreshSpaceState();
        this.viewCardLayout.show(this.viewPanel, "systemMap");
        updateBottomControls();
        this.systemMapPanel.requestFocusInWindow();
        this.systemMapPanel.repaint();
    }

    private void selectStarMapView() {
        this.activeView = "starMap";
        this.viewCardLayout.show(this.viewPanel, "starMap");
        updateBottomControls();
        this.starMapPanel.repaint();
    }

    private void selectStationView() {
        this.activeView = "station";
        this.viewCardLayout.show(this.viewPanel, "station");
        updateBottomControls();
        this.stationPanel.repaint();
    }

    private void updateBottomControls() {
        boolean showSpaceControls = this.stationState == null || !this.stationState.isDocked();
        this.controlsCardLayout.show(this.controlsPanel, showSpaceControls ? "space" : "docked");
        if ("radar".equals(this.activeView)) {
            this.statusLabel.setText("Radar scope active");
        } else if ("systemMap".equals(this.activeView)) {
            this.statusLabel.setText("System map active - arrow keys pan the view");
        } else if ("starMap".equals(this.activeView)) {
            this.statusLabel.setText("Star map active");
        } else {
            this.statusLabel.setText("Docked and in station");
        }
        this.frame.revalidate();
        this.frame.repaint();
    }

    private void refreshSpaceState() {
        if (this.loggedInUsername == null) {
            return;
        }

        this.systemMapState = this.server.getSystemMapState(this.loggedInUsername);
        if (this.systemMapState == null) {
            return;
        }

        this.radarRangeLabel.setText("Radar distance: " + this.selectedRadarRangeKm + " km");
        this.systemDetailsArea.setText(buildSystemDetailsText());
        this.characterDetailsArea.setText(buildCharacterDetailsText());
        this.flightPlanDetailsArea.setText(buildFlightPlanDetailsText());
        this.systemMapPanel.repaint();
    }

    private void refreshDockedState() {
        if (this.loggedInUsername == null) {
            return;
        }
        this.stationState = this.server.getStationState(this.loggedInUsername);
        if (this.stationState == null) {
            return;
        }
        this.playerLabel.setText("Pilot: " + (this.stationState.getPlayerName() == null || this.stationState.getPlayerName().isBlank()
                ? this.loggedInUsername
                : this.stationState.getPlayerName()));
        this.locationLabel.setText("Docked at: " + this.stationState.getStationName() + " / " + this.stationState.getSystemName());
        this.statusLabel.setText("Docked and in station");
        updateDockedTaskButtons();
        this.stationPanel.repaint();
    }

    private void updateDockedTaskButtons() {
        boolean hasCharacter = this.stationState != null && this.stationState.isCharacterCreated();
        this.characterButton.setText(hasCharacter ? "Character" : "Create Character");
        this.characterButton.setEnabled(true);
    }

    private void openCharacterCreationDialog() {
        if (this.stationState == null) {
            this.stationState = this.server.getStationState(this.loggedInUsername);
        }

        JDialog dialog = new JDialog(this.frame, "Create Character", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(460, 260);
        dialog.setLocationRelativeTo(this.frame);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(18, 24, 36));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        JLabel heading = new JLabel("Create your pilot");
        heading.setForeground(Color.WHITE);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 18f));
        c.gridwidth = 2;
        form.add(heading, c);

        c.gridwidth = 1;
        c.gridy++;
        JLabel nameLabel = new JLabel("Character name");
        nameLabel.setForeground(Color.WHITE);
        form.add(nameLabel, c);

        JTextField nameField = new JTextField(this.loggedInUsername, 18);
        c.gridx = 1;
        form.add(nameField, c);

        c.gridy++;
        c.gridx = 0;
        JLabel shipLabel = new JLabel("Ship");
        shipLabel.setForeground(Color.WHITE);
        form.add(shipLabel, c);

        JComboBox<String> shipBox = new JComboBox<>(this.stationState == null ? new String[] {"Shuttle"} : this.stationState.getShipOptions());
        c.gridx = 1;
        form.add(shipBox, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            CharacterCreationResult creation = this.server.createCharacter(
                    this.loggedInUsername,
                    nameField.getText().trim(),
                    (String) shipBox.getSelectedItem()
            );
            if (!creation.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, creation.getMessage(), "Character creation", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.activePlayer = creation.getPlayerCharacter();
            this.stationState = this.server.getStationState(this.loggedInUsername);
            refreshDockedState();
            selectStationView();
            this.rootCardLayout.show(this.rootPanel, "game");
            dialog.dispose();
        });
        form.add(createButton, c);

        dialog.add(form, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void handleUndock() {
        if (this.loggedInUsername == null) {
            return;
        }
        this.server.undock(this.loggedInUsername);
        this.stationState = this.server.getStationState(this.loggedInUsername);
        refreshSpaceState();
        selectRadarView();
        this.rootCardLayout.show(this.rootPanel, "game");
    }

    private void openMarketMenu() {
        StationState state = this.server.getStationState(this.loggedInUsername);
        if (state == null) {
            return;
        }
        JDialog dialog = new JDialog(this.frame, "Market", true);
        dialog.setSize(640, 420);
        dialog.setLocationRelativeTo(this.frame);
        JTextArea marketArea = createReadOnlyArea();
        StringBuilder text = new StringBuilder();
        celestialsons.market.Contract[] contracts = state.getMarketContracts();
        if (contracts != null) {
            for (celestialsons.market.Contract contract : contracts) {
                text.append(contract.getItemName())
                        .append(" x").append(contract.getQuantity())
                        .append(" @ ").append(contract.getPricePerUnit())
                        .append(" from ").append(contract.getSellingParty())
                        .append('\n');
            }
        }
        marketArea.setText(text.toString());
        dialog.add(new JScrollPane(marketArea), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void openShipyardMenu() {
        StationState state = this.server.getStationState(this.loggedInUsername);
        if (state == null) {
            return;
        }
        JOptionPane.showMessageDialog(
                this.frame,
                "Available ships:\n" + String.join(", ", state.getShipOptions()),
                "Shipyard",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void openCharacterSheet() {
        if (this.stationState == null || !this.stationState.isCharacterCreated() || this.activePlayer == null) {
            openCharacterCreationDialog();
            return;
        }
        String shipType = this.activePlayer == null ? "Unknown" : this.activePlayer.getShipType();
        JOptionPane.showMessageDialog(
                this.frame,
                "Pilot: " + (this.activePlayer == null ? this.loggedInUsername : this.activePlayer.getName()) + "\nShip: " + shipType,
                "Character",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String buildSystemDetailsText() {
        if (this.systemMapState == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        text.append("Server: ").append(this.systemMapState.getServerName()).append('\n');
        text.append("System: ").append(this.systemMapState.getSystemName()).append('\n');
        text.append("Player position: ").append(formatPosition(this.systemMapState.getPlayerPosition())).append('\n');
        text.append("Orbital bodies:\n");
        for (MapMarker marker : this.systemMapState.getOrbitalBodies()) {
            text.append(" - ").append(marker.getType()).append(" ").append(marker.getName())
                    .append(" @ ").append(formatPosition(marker.getPosition())).append('\n');
        }
        return text.toString();
    }

    private String buildCharacterDetailsText() {
        if (this.systemMapState == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (CharacterMarker marker : this.systemMapState.getCharacters()) {
            text.append(marker.getName());
            text.append(marker.isTransponderActive() ? " [transponder active]" : " [transponder off]");
            text.append(" @ ").append(formatPosition(marker.getPosition())).append('\n');
        }
        return text.toString();
    }

    private String buildFlightPlanDetailsText() {
        if (this.systemMapState == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (FlightPlanMarker marker : this.systemMapState.getFlightPlans()) {
            text.append(marker.getOwnerName()).append(": ").append(marker.getLabel()).append('\n');
            DimensionalPosition[] waypoints = marker.getWaypoints();
            for (int i = 0; i < waypoints.length; i++) {
                text.append("  ").append(i + 1).append(". ").append(formatPosition(waypoints[i])).append('\n');
            }
        }
        return text.toString();
    }

    private String formatPosition(DimensionalPosition position) {
        if (position == null) {
            return "(unknown)";
        }
        return String.format("(%.1f, %.1f, %.1f)", position.getX(), position.getY(), position.getZ());
    }

    private void refreshActiveState() {
        if (this.loggedInUsername == null || "login".equals(this.activeView)) {
            return;
        }
        if ("station".equals(this.activeView)) {
            refreshDockedState();
        } else {
            refreshSpaceState();
        }
    }

    private double distance(DimensionalPosition a, DimensionalPosition b) {
        return Math.sqrt(distanceSquared(a, b));
    }

    private double distanceSquared(DimensionalPosition a, DimensionalPosition b) {
        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    private class RadarPanel extends JPanel {
        private final Timer sweepTimer;
        private double sweepAngle;

        RadarPanel() {
            setBackground(new Color(6, 10, 16));
            this.sweepTimer = new Timer(40, e -> {
                this.sweepAngle = (this.sweepAngle + 0.03) % (Math.PI * 2.0);
                repaint();
            });
            this.sweepTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.max(120, Math.min(width, height) / 2 - 84);
            DimensionalPosition origin = this.resolvePlayerOrigin();

            g2.setColor(new Color(8, 13, 20));
            g2.fillRect(0, 0, width, height);

            drawRadarFrame(g2, centerX, centerY, radius);
            drawRadarGrid(g2, centerX, centerY, radius);
            drawRadarSweep(g2, centerX, centerY, radius);
            drawRadarObjects(g2, centerX, centerY, radius, origin);
            drawRadarShip(g2, centerX, centerY);
            drawRadarScaleLabels(g2, centerX, centerY, radius);
            drawRadarTelemetry(g2, centerX, centerY, radius);

            g2.dispose();
        }

        private DimensionalPosition resolvePlayerOrigin() {
            if (activePlayer != null && activePlayer.getCurrentLocation() != null) {
                return activePlayer.getCurrentLocation();
            }
            return new DimensionalPosition(0.0, 0.0, 0.0);
        }

        private void drawRadarFrame(Graphics2D g2, int centerX, int centerY, int radius) {
            g2.setColor(new Color(14, 24, 20));
            g2.fillOval(centerX - radius - 14, centerY - radius - 14, (radius + 14) * 2, (radius + 14) * 2);

            g2.setColor(new Color(48, 173, 95));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(centerX - radius + 14, centerY - radius + 14, (radius - 14) * 2, (radius - 14) * 2);
            g2.drawOval(centerX - radius + 28, centerY - radius + 28, (radius - 28) * 2, (radius - 28) * 2);
            g2.drawLine(centerX - radius, centerY, centerX + radius, centerY);
            g2.drawLine(centerX, centerY - radius, centerX, centerY + radius);
            g2.drawLine(centerX - (int) (radius * 0.707), centerY - (int) (radius * 0.707), centerX + (int) (radius * 0.707), centerY + (int) (radius * 0.707));
            g2.drawLine(centerX - (int) (radius * 0.707), centerY + (int) (radius * 0.707), centerX + (int) (radius * 0.707), centerY - (int) (radius * 0.707));
        }

        private void drawRadarGrid(Graphics2D g2, int centerX, int centerY, int radius) {
            g2.setColor(new Color(48, 173, 95, 80));
            g2.setStroke(new BasicStroke(1f));

            for (int i = 1; i < 6; i++) {
                int ringRadius = (int) Math.round(radius * (i / 6.0));
                g2.drawOval(centerX - ringRadius, centerY - ringRadius, ringRadius * 2, ringRadius * 2);
            }

            for (int i = 1; i < 12; i++) {
                double angle = (Math.PI * 2.0 * i) / 12.0;
                int innerX = centerX + (int) Math.round(Math.cos(angle) * (radius * 0.12));
                int innerY = centerY + (int) Math.round(Math.sin(angle) * (radius * 0.12));
                int outerX = centerX + (int) Math.round(Math.cos(angle) * radius);
                int outerY = centerY + (int) Math.round(Math.sin(angle) * radius);
                g2.drawLine(innerX, innerY, outerX, outerY);
            }
        }

        private void drawRadarSweep(Graphics2D g2, int centerX, int centerY, int radius) {
            int sweepX = centerX + (int) Math.round(Math.cos(this.sweepAngle) * radius);
            int sweepY = centerY + (int) Math.round(Math.sin(this.sweepAngle) * radius);
            g2.setColor(new Color(96, 255, 140, 110));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(centerX, centerY, sweepX, sweepY);
            g2.setColor(new Color(96, 255, 140, 35));
            for (int i = 1; i <= 3; i++) {
                double angle = this.sweepAngle - (i * 0.10);
                int x = centerX + (int) Math.round(Math.cos(angle) * radius);
                int y = centerY + (int) Math.round(Math.sin(angle) * radius);
                g2.drawLine(centerX, centerY, x, y);
            }
        }

        private void drawRadarObjects(Graphics2D g2, int centerX, int centerY, int radius, DimensionalPosition origin) {
            if (systemMapState == null) {
                return;
            }

            for (MapMarker marker : systemMapState.getOrbitalBodies()) {
                drawRadarMarker(g2, marker.getPosition(), marker.getType(), centerX, centerY, radius, origin);
            }
            for (CharacterMarker marker : systemMapState.getCharacters()) {
                drawRadarMarker(g2, marker.getPosition(), marker.isTransponderActive() ? "Character" : "Silent", centerX, centerY, radius, origin);
            }
        }

        private void drawRadarMarker(Graphics2D g2, DimensionalPosition world, String type, int centerX, int centerY, int radius, DimensionalPosition origin) {
            if (world == null) {
                return;
            }
            double dx = world.getX() - origin.getX();
            double dz = world.getZ() - origin.getZ();
            double flatDistance = Math.sqrt((dx * dx) + (dz * dz));
            if (flatDistance > selectedRadarRangeKm) {
                return;
            }

            double normalized = flatDistance / Math.max(1.0, selectedRadarRangeKm);
            int blipDistance = (int) Math.round(normalized * radius);
            double angle = Math.atan2(dz, dx);
            int x = centerX + (int) Math.round(Math.cos(angle) * blipDistance);
            int y = centerY + (int) Math.round(Math.sin(angle) * blipDistance);
            double dy = world.getY() - origin.getY();
            y -= (int) Math.round(Math.max(-24.0, Math.min(24.0, dy / 6.0)));

            Color markerColor;
            int size;
            if ("Star".equals(type)) {
                markerColor = new Color(255, 225, 120);
                size = 9;
            } else if ("Planet".equals(type)) {
                markerColor = new Color(110, 220, 255);
                size = 7;
            } else if ("Moon".equals(type)) {
                markerColor = new Color(160, 240, 255);
                size = 5;
            } else if ("Station".equals(type)) {
                markerColor = new Color(112, 255, 148);
                size = 7;
            } else {
                markerColor = new Color(255, 120, 120);
                size = 6;
            }

            int elevation = (int) Math.round(dy);
            int elevationBarHeight = Math.max(3, Math.min(18, Math.abs(elevation) / 8));
            int elevationOffset = Integer.compare(elevation, 0) * (size + 3);

            g2.setColor(new Color(12, 18, 16, 160));
            g2.fillOval(x - size - 2, y - size - 2, (size * 2) + 4, (size * 2) + 4);
            g2.setColor(markerColor);
            g2.fillOval(x - size / 2, y - size / 2, size, size);

            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(x, y + elevationOffset, x, y + elevationOffset - elevationBarHeight * Integer.signum(elevation == 0 ? 1 : elevation));
        }

        private void drawRadarShip(Graphics2D g2, int centerX, int centerY) {
            Polygon shipTriangle = new Polygon();
            shipTriangle.addPoint(centerX, centerY - 12);
            shipTriangle.addPoint(centerX - 10, centerY + 10);
            shipTriangle.addPoint(centerX + 10, centerY + 10);
            g2.setColor(new Color(255, 234, 120));
            g2.fillPolygon(shipTriangle);
            g2.setColor(new Color(255, 248, 196));
            g2.drawPolygon(shipTriangle);
            g2.fillOval(centerX - 2, centerY - 2, 4, 4);
        }

        private void drawRadarScaleLabels(Graphics2D g2, int centerX, int centerY, int radius) {
            g2.setColor(new Color(94, 232, 133));
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            int[] rings = rangeValues();
            for (int i = 0; i < rings.length; i++) {
                int ringRadius = (int) Math.round(radius * ((i + 1) / 6.0));
                g2.drawString(rings[i] + " km", centerX + ringRadius - 8, centerY - ringRadius - 6);
            }
        }

        private void drawRadarTelemetry(Graphics2D g2, int centerX, int centerY, int radius) {
            g2.setColor(new Color(94, 232, 133));
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 13f));
            g2.drawString("ELITE SCAN", centerX - radius + 10, centerY + radius + 22);
            g2.drawString("RANGE " + selectedRadarRangeKm + " KM", centerX + radius - 132, centerY + radius + 22);
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

    private class SystemMapPanel extends JPanel {
        SystemMapPanel() {
            setBackground(new Color(6, 10, 16));
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            installKeyBindings();
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    systemMapMousePoint = e.getPoint();
                    hoveredOrbitalLabel = findHoveredOrbitalLabel(e.getPoint());
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    mouseMoved(e);
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMousePress(e);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        handleMousePress(e);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    systemMapMousePoint = null;
                    hoveredOrbitalLabel = null;
                    repaint();
                }
            });
            addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    handleZoom(e);
                }
            });
        }

        private void installKeyBindings() {
            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getActionMap();

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "panLeft");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "panRight");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "panUp");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "panDown");

            actionMap.put("panLeft", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mapPanX -= SYSTEM_MAP_PAN_STEP;
                    repaintMap();
                }
            });
            actionMap.put("panRight", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mapPanX += SYSTEM_MAP_PAN_STEP;
                    repaintMap();
                }
            });
            actionMap.put("panUp", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mapPanY -= SYSTEM_MAP_PAN_STEP;
                    repaintMap();
                }
            });
            actionMap.put("panDown", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mapPanY += SYSTEM_MAP_PAN_STEP;
                    repaintMap();
                }
            });
        }

        private void repaintMap() {
            refreshSpaceState();
            repaint();
        }

        private void handleZoom(MouseWheelEvent e) {
            if (e.isShiftDown()) {
                return;
            }
            systemMapZoom = clamp(systemMapZoom - (e.getPreciseWheelRotation() * 0.08), 0.35, 4.0);
            repaintMap();
        }

        private void handleMousePress(MouseEvent e) {
            if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                openSystemMapContextMenu(e.getPoint());
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                handleSystemMapClick(e.getPoint());
            }
        }

        private void handleSystemMapClick(Point point) {
            if (systemMapState == null) {
                return;
            }
            MapMarker marker = findOrbitalMarker(point);
            if (marker != null) {
                if ("Station".equals(marker.getType()) && canDockAtStation(marker)) {
                    openStationContextMenu(marker, point);
                    return;
                }
                requestFlightPlan(marker.getPosition());
                return;
            }

            DimensionalPosition destination = screenToWorld(point);
            requestFlightPlan(destination);
        }

        private void openSystemMapContextMenu(Point point) {
            MapMarker marker = findOrbitalMarker(point);
            if (marker == null) {
                return;
            }
            if ("Station".equals(marker.getType()) && canDockAtStation(marker)) {
                openStationContextMenu(marker, point);
                return;
            }
            JPopupMenu menu = new JPopupMenu();
            JMenuItem filePlan = new JMenuItem("File Flight Plan");
            filePlan.addActionListener(e -> requestFlightPlan(marker.getPosition()));
            menu.add(filePlan);
            menu.show(this, point.x, point.y);
        }

        private void openStationContextMenu(MapMarker marker, Point point) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem dockItem = new JMenuItem("Dock");
            dockItem.addActionListener(e -> {
                if (server.dockAtStation(loggedInUsername, marker.getName())) {
                    refreshDockedState();
                    selectStationView();
                    rootCardLayout.show(rootPanel, "game");
                }
            });
            JMenuItem flightItem = new JMenuItem("Fly to Station Approach");
            flightItem.addActionListener(e -> requestFlightPlan(marker.getPosition()));
            menu.add(dockItem);
            menu.add(flightItem);
            menu.show(this, point.x, point.y);
        }

        private boolean requestFlightPlan(DimensionalPosition destination) {
            if (destination == null || loggedInUsername == null) {
                return false;
            }
            boolean success = server.fileFlightPlan(loggedInUsername, destination);
            if (success) {
                refreshSpaceState();
                selectSystemMapView();
            }
            return success;
        }

        private boolean canDockAtStation(MapMarker marker) {
            if (systemMapState == null || marker == null || marker.getPosition() == null) {
                return false;
            }
            return distance(systemMapState.getPlayerPosition(), marker.getPosition()) <= 30.0;
        }

        private MapMarker findOrbitalMarker(Point point) {
            if (systemMapState == null || point == null) {
                return null;
            }
            double viewportCenterX = getViewportCenterX();
            double viewportCenterY = getViewportCenterY();
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            MapMarker best = null;
            double bestDistance = Double.MAX_VALUE;
            for (MapMarker marker : systemMapState.getOrbitalBodies()) {
                Point markerPoint = project(marker.getPosition(), viewportCenterX, viewportCenterY, centerX, centerY);
                double distance = point.distance(markerPoint);
                if (distance < 16.0 && distance < bestDistance) {
                    best = marker;
                    bestDistance = distance;
                }
            }
            return best;
        }

        private double getViewportCenterX() {
            return systemMapState.getPlayerPosition().getX() + mapPanX;
        }

        private double getViewportCenterY() {
            return systemMapState.getPlayerPosition().getY() + mapPanY;
        }

        private DimensionalPosition screenToWorld(Point point) {
            double scale = SYSTEM_MAP_SCALE * systemMapZoom;
            return new DimensionalPosition(
                    getViewportCenterX() + ((point.x - (getWidth() / 2.0)) / scale),
                    getViewportCenterY() + ((point.y - (getHeight() / 2.0)) / scale),
                    systemMapState.getPlayerPosition().getZ()
            );
        }

        private String findHoveredOrbitalLabel(Point mousePoint) {
            if (systemMapState == null || mousePoint == null) {
                return null;
            }

            double bestDistance = Double.MAX_VALUE;
            String bestLabel = null;
            double viewportCenterX = getViewportCenterX();
            double viewportCenterY = getViewportCenterY();

            for (MapMarker marker : systemMapState.getOrbitalBodies()) {
                Point point = project(marker.getPosition(), viewportCenterX, viewportCenterY, getWidth() / 2, getHeight() / 2);
                double distance = mousePoint.distance(point);
                if (distance < 14.0 && distance < bestDistance) {
                    bestDistance = distance;
                    bestLabel = marker.getName();
                }
            }
            return bestLabel;
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

            g2.setColor(new Color(8, 13, 20));
            g2.fillRect(0, 0, width, height);

            if (systemMapState == null) {
                g2.setColor(Color.WHITE);
                g2.drawString("No system map data available.", 20, 24);
                g2.dispose();
                return;
            }

            DimensionalPosition playerPosition = systemMapState.getPlayerPosition();
            double viewportCenterX = playerPosition.getX() + mapPanX;
            double viewportCenterY = playerPosition.getY() + mapPanY;

            drawGrid(g2, centerX, centerY, viewportCenterX, viewportCenterY, width, height);
            drawOrbitalBodies(g2, centerX, centerY, viewportCenterX, viewportCenterY);
            drawFlightPlans(g2, centerX, centerY, viewportCenterX, viewportCenterY);
            drawCharacters(g2, centerX, centerY, viewportCenterX, viewportCenterY);

            g2.setColor(new Color(172, 184, 204));
            g2.drawString("Pan: " + (int) mapPanX + ", " + (int) mapPanY, 16, 22);
            g2.dispose();
        }

        private void drawGrid(Graphics2D g2, int centerX, int centerY, double viewportCenterX, double viewportCenterY, int width, int height) {
            g2.setColor(new Color(24, 34, 48));
            int spacing = Math.max(60, (int) Math.round(120 * systemMapZoom));
            for (int x = centerX % spacing; x < width; x += spacing) {
                g2.drawLine(x, 0, x, height);
            }
            for (int y = centerY % spacing; y < height; y += spacing) {
                g2.drawLine(0, y, width, y);
            }

            g2.setColor(new Color(45, 62, 84));
            g2.drawLine(centerX, 0, centerX, height);
            g2.drawLine(0, centerY, width, centerY);
            g2.drawString("Viewport center: " + formatPosition(new DimensionalPosition(viewportCenterX, viewportCenterY, 0.0)), 16, 42);
        }

        private void drawOrbitalBodies(Graphics2D g2, int centerX, int centerY, double viewportCenterX, double viewportCenterY) {
            List<PlacedMarker> markers = new java.util.ArrayList<>();
            for (MapMarker marker : systemMapState.getOrbitalBodies()) {
                Point point = project(marker.getPosition(), viewportCenterX, viewportCenterY, centerX, centerY);
                markers.add(new PlacedMarker(marker, point));
                if ("Star".equals(marker.getType())) {
                    g2.setColor(new Color(255, 211, 100));
                    g2.fillOval(point.x - 10, point.y - 10, 20, 20);
                    g2.setColor(Color.WHITE);
                } else if ("Planet".equals(marker.getType())) {
                    g2.setColor(new Color(118, 168, 255));
                    g2.fillOval(point.x - 6, point.y - 6, 12, 12);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(203, 211, 220));
                    g2.fillOval(point.x - 3, point.y - 3, 6, 6);
                    g2.setColor(Color.WHITE);
                }
            }
            drawOrbitalLabels(g2, markers);
            if (hoveredOrbitalLabel != null) {
                drawHoverHighlight(g2, markers);
            }
        }

        private void drawOrbitalLabels(Graphics2D g2, List<PlacedMarker> markers) {
            Font originalFont = g2.getFont();
            Point hoveredPoint = getHoveredPoint(markers);
            for (PlacedMarker placed : markers) {
                MapMarker marker = placed.marker;
                boolean important = isImportantOrbitalType(marker.getType());
                boolean hovered = marker.getName().equals(hoveredOrbitalLabel);
                boolean nearCursor = systemMapMousePoint != null && systemMapMousePoint.distance(placed.point) < 120.0;

                if (!important && !hovered) {
                    continue;
                }

                if (hovered) {
                    g2.setFont(originalFont.deriveFont(Font.BOLD, 14f));
                    g2.setColor(new Color(244, 248, 255));
                } else if (nearCursor) {
                    g2.setFont(originalFont.deriveFont(Font.PLAIN, 12f));
                    g2.setColor(new Color(172, 184, 204, 120));
                } else if (important) {
                    g2.setFont(originalFont.deriveFont(Font.PLAIN, 12f));
                    g2.setColor(new Color(235, 238, 244));
                }

                g2.drawString(marker.getName(), placed.point.x + 8, placed.point.y - 8);
            }
            g2.setFont(originalFont);
        }

        private void drawHoverHighlight(Graphics2D g2, List<PlacedMarker> markers) {
            Point hoveredPoint = getHoveredPoint(markers);
            if (hoveredPoint == null) {
                return;
            }
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawOval(hoveredPoint.x - 14, hoveredPoint.y - 14, 28, 28);
        }

        private boolean isImportantOrbitalType(String type) {
            return "Star".equals(type) || "Planet".equals(type) || "Station".equals(type);
        }

        private Point getHoveredPoint(List<PlacedMarker> markers) {
            if (hoveredOrbitalLabel == null) {
                return null;
            }
            for (PlacedMarker marker : markers) {
                if (hoveredOrbitalLabel.equals(marker.marker.getName())) {
                    return marker.point;
                }
            }
            return null;
        }

        private void drawCharacters(Graphics2D g2, int centerX, int centerY, double viewportCenterX, double viewportCenterY) {
        for (CharacterMarker marker : systemMapState.getCharacters()) {
            Point point = project(marker.getPosition(), viewportCenterX, viewportCenterY, centerX, centerY);
            if (activePlayer != null && marker.getName().equals(activePlayer.getName())) {
                Polygon triangle = new Polygon();
                triangle.addPoint(point.x, point.y - 9);
                triangle.addPoint(point.x - 8, point.y + 9);
                triangle.addPoint(point.x + 8, point.y + 9);
                g2.setColor(new Color(255, 226, 95));
                    g2.fillPolygon(triangle);
                    g2.setColor(new Color(255, 245, 171));
                    g2.drawPolygon(triangle);
                    g2.drawString(marker.getName() + " (you)", point.x + 10, point.y + 4);
                } else {
                    g2.setColor(marker.isTransponderActive() ? new Color(92, 255, 155) : new Color(122, 137, 153));
                    g2.fillOval(point.x - 5, point.y - 5, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.drawString(marker.getName(), point.x + 10, point.y + 4);
                }
            }
        }

        private void drawFlightPlans(Graphics2D g2, int centerX, int centerY, double viewportCenterX, double viewportCenterY) {
        g2.setStroke(new BasicStroke(1.5f));
        for (FlightPlanMarker marker : systemMapState.getFlightPlans()) {
            DimensionalPosition[] waypoints = marker.getWaypoints();
            if (waypoints.length == 0) {
                continue;
            }
            Color planColor = activePlayer != null && marker.getOwnerName().equals(activePlayer.getName())
                    ? new Color(255, 214, 102)
                    : new Color(118, 255, 198);
                g2.setColor(planColor);
                Point previous = null;
                for (DimensionalPosition waypoint : waypoints) {
                    Point current = project(waypoint, viewportCenterX, viewportCenterY, centerX, centerY);
                    g2.fillOval(current.x - 3, current.y - 3, 6, 6);
                    if (previous != null) {
                        g2.drawLine(previous.x, previous.y, current.x, current.y);
                    }
                    previous = current;
                }
                Point labelPoint = project(waypoints[0], viewportCenterX, viewportCenterY, centerX, centerY);
                g2.drawString(marker.getOwnerName() + " plan", labelPoint.x + 10, labelPoint.y + 15);
            }
        }

        private Point project(DimensionalPosition world, double viewportCenterX, double viewportCenterY, int centerX, int centerY) {
            double scale = SYSTEM_MAP_SCALE * systemMapZoom;
            int x = centerX + (int) Math.round((world.getX() - viewportCenterX) * scale);
            int y = centerY + (int) Math.round((world.getY() - viewportCenterY) * scale);
            return new Point(x, y);
        }

        private class PlacedMarker {
            private final MapMarker marker;
            private final Point point;

            private PlacedMarker(MapMarker marker, Point point) {
                this.marker = marker;
                this.point = point;
            }
        }
    }

    private class StarMapPanel extends JPanel {
        StarMapPanel() {
            setBackground(new Color(6, 10, 16));
            addMouseWheelListener(e -> {
                starMapZoom = Math.max(0.35, Math.min(4.0, starMapZoom - (e.getPreciseWheelRotation() * 0.08)));
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(8, 13, 20));
            g2.fillRect(0, 0, width, height);

            Universe universe = server.getUniverse();
            Star[] stars = universe == null ? null : universe.getStarList();
            if (stars == null || stars.length == 0) {
                g2.setColor(Color.WHITE);
                g2.drawString("No star map data available.", 20, 24);
                g2.dispose();
                return;
            }

            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            for (Star star : stars) {
                if (star == null || star.getPosition() == null) {
                    continue;
                }
                minX = Math.min(minX, star.getPosition().getX());
                maxX = Math.max(maxX, star.getPosition().getX());
                minY = Math.min(minY, star.getPosition().getY());
                maxY = Math.max(maxY, star.getPosition().getY());
            }
            double mapWidth = Math.max(1.0, maxX - minX);
            double mapHeight = Math.max(1.0, maxY - minY);
            double scale = Math.min((width - 120.0) / mapWidth, (height - 120.0) / mapHeight) * starMapZoom;
            if (Double.isInfinite(scale) || Double.isNaN(scale) || scale <= 0) {
                scale = 1.0;
            }

            int offsetX = 60;
            int offsetY = 60;
            String currentSystemName = activePlayer != null && activePlayer.getCurrentSystem() != null
                    ? activePlayer.getCurrentSystem().getName()
                    : (stationState != null ? stationState.getSystemName() : "");

            g2.setColor(new Color(173, 184, 205));
            g2.drawString("Star Map", 20, 24);
            g2.drawString("Systems loaded: " + stars.length, 20, 42);
            if (!currentSystemName.isBlank()) {
                g2.drawString("Current system: " + currentSystemName, 20, 60);
            }

            for (Star star : stars) {
                if (star == null || star.getPosition() == null) {
                    continue;
                }
                int x = offsetX + (int) Math.round((star.getPosition().getX() - minX) * scale);
                int y = offsetY + (int) Math.round((star.getPosition().getY() - minY) * scale);
                boolean current = star.getName() != null && star.getName().equals(currentSystemName);
                g2.setColor(current ? new Color(255, 221, 102) : new Color(118, 168, 255));
                int size = current ? 12 : 7;
                g2.fillOval(x - size / 2, y - size / 2, size, size);
                g2.setColor(Color.WHITE);
                g2.drawString(star.getName() + " (" + (star.getPlanets() == null ? 0 : star.getPlanets().length) + ")", x + 8, y - 4);
            }

            g2.dispose();
        }
    }

    private class StationPanel extends JPanel {
        StationPanel() {
            super(new BorderLayout());
            add(buildStationPanel(), BorderLayout.CENTER);
        }
    }

    private class StationSilhouettePanel extends JPanel {
        StationSilhouettePanel() {
            setBackground(new Color(8, 12, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(8, 12, 18));
            g2.fillRect(0, 0, width, height);

            String stationName = GameClient.this.stationState == null ? "Station" : GameClient.this.stationState.getStationName();
            String systemName = GameClient.this.stationState == null ? "Unknown System" : GameClient.this.stationState.getSystemName();

            g2.setColor(new Color(255, 255, 255, 18));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 76f));
            FontMetrics metrics = g2.getFontMetrics();
            int textWidth = metrics.stringWidth(stationName);
            g2.drawString(stationName, Math.max(20, (width - textWidth) / 2), (height / 2));

            g2.setColor(new Color(255, 255, 255, 35));
            g2.setStroke(new BasicStroke(3f));
            int ringRadius = Math.min(width, height) / 3;
            g2.drawOval((width / 2) - ringRadius, (height / 2) - ringRadius, ringRadius * 2, ringRadius * 2);
            g2.drawLine(width / 2 - ringRadius, height / 2, width / 2 + ringRadius, height / 2);

            g2.setColor(new Color(235, 238, 244));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
            g2.drawString(systemName, 24, 32);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
            g2.drawString("Docked at " + stationName, 24, 56);
            if (GameClient.this.stationState != null && GameClient.this.stationState.getPlayerName() != null && !GameClient.this.stationState.getPlayerName().isBlank()) {
                g2.drawString("Pilot: " + GameClient.this.stationState.getPlayerName(), 24, 80);
                g2.drawString("Ship: " + GameClient.this.stationState.getShipType(), 24, 104);
            }

            g2.dispose();
        }
    }
}
