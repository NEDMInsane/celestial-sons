package celestialsons.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import celestialsons.DimensionalPosition;
import celestialsons.MapMarker;
import celestialsons.CharacterMarker;
import celestialsons.PlayerCharacter;

public class RadarPanel extends JPanel {
    private final Timer sweepTimer;
    private double sweepAngle;
    private PlayerCharacter player;
    private List<MapMarker> orbitalBodies = new ArrayList<>();
    private List<CharacterMarker> characters = new ArrayList<>();
    private int selectedRadarRangeKm = 100;

    // 3D visualization parameters
    private static final int GRID_LINES = 8;
    private static final Color GRID_COLOR = new Color(48, 173, 95, 100);
    private static final Color SWEEP_COLOR = new Color(96, 255, 140, 180);
    private static final Color PLAYER_COLOR = new Color(255, 234, 120);

    public RadarPanel() {
        setBackground(new Color(6, 10, 16));
        this.sweepTimer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sweepAngle = (sweepAngle + 0.03) % (Math.PI * 2.0);
                repaint();
            }
        });
        this.sweepTimer.start();
    }

    public void updateData(PlayerCharacter player, List<MapMarker> orbitalBodies,
                           List<CharacterMarker> characters, int selectedRadarRangeKm) {
        this.player = player;
        this.orbitalBodies = orbitalBodies != null ? orbitalBodies : new ArrayList<>();
        this.characters = characters != null ? characters : new ArrayList<>();
        this.selectedRadarRangeKm = selectedRadarRangeKm;
        repaint();
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
        int radius = Math.max(120, Math.min(width, height) / 2 - 50);

        DimensionalPosition playerPosition = getPlayerPosition();

        // Draw background
        g2.setColor(new Color(8, 13, 20));
        g2.fillRect(0, 0, width, height);

        // Draw 3D radar grid (cylindrical representation)
        draw3DRadarGrid(g2, centerX, centerY, radius);

        // Draw radar sweep
        drawRadarSweep(g2, centerX, centerY, radius);

        // Draw objects with 3D representation
        drawRadarObjects(g2, centerX, centerY, radius, playerPosition);

        // Draw player ship
        drawPlayerShip(g2, centerX, centerY);

        // Draw telemetry information
        drawTelemetry(g2, centerX, centerY, radius);

        g2.dispose();
    }

    private DimensionalPosition getPlayerPosition() {
        if (player != null && player.getCurrentLocation() != null) {
            return player.getCurrentLocation();
        }
        return new DimensionalPosition(0.0, 0.0, 0.0);
    }

    private void draw3DRadarGrid(Graphics2D g2, int centerX, int centerY, int radius) {
        // Draw outer circle
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw concentric circles (range indicators)
        g2.setStroke(new BasicStroke(1f));
        for (int i = 1; i < 5; i++) {
            int r = radius * i / 5;
            g2.drawOval(centerX - r, centerY - r, r * 2, r * 2);
        }

        // Draw grid lines (radial)
        for (int i = 0; i < GRID_LINES; i++) {
            double angle = (Math.PI * 2 * i) / GRID_LINES;
            int x1 = centerX + (int) (Math.cos(angle) * (radius * 0.2));
            int y1 = centerY + (int) (Math.sin(angle) * (radius * 0.2));
            int x2 = centerX + (int) (Math.cos(angle) * radius);
            int y2 = centerY + (int) (Math.sin(angle) * radius);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Draw elevation markers (vertical lines)
        g2.setColor(new Color(48, 173, 95, 60));
        for (int i = -2; i <= 2; i++) {
            if (i == 0) continue; // Skip center line
            int y = centerY + (i * radius / 3);
            g2.drawLine(centerX - radius, y, centerX + radius, y);
        }

        // Draw center horizontal line
        g2.setColor(GRID_COLOR);
        g2.drawLine(centerX - radius, centerY, centerX + radius, centerY);
    }

    private void drawRadarSweep(Graphics2D g2, int centerX, int centerY, int radius) {
        // Draw sweep line
        int sweepX = centerX + (int) (Math.cos(sweepAngle) * radius);
        int sweepY = centerY + (int) (Math.sin(sweepAngle) * radius);
        g2.setColor(SWEEP_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(centerX, centerY, sweepX, sweepY);

        // Draw sweep arc
        g2.setColor(new Color(96, 255, 140, 30));
        for (int i = 1; i <= 3; i++) {
            double angle = sweepAngle - (i * 0.15);
            int x = centerX + (int) (Math.cos(angle) * radius);
            int y = centerY + (int) (Math.sin(angle) * radius);
            g2.drawLine(centerX, centerY, x, y);
        }
    }

    private void drawRadarObjects(Graphics2D g2, int centerX, int centerY, int radius, DimensionalPosition playerPosition) {
        // Draw orbital bodies
        for (MapMarker marker : orbitalBodies) {
            drawObject(g2, marker.getPosition(), marker.getType(), centerX, centerY, radius, playerPosition);
        }

        // Draw characters/ships
        for (CharacterMarker marker : characters) {
            drawObject(g2, marker.getPosition(),
                    marker.isTransponderActive() ? "Character" : "Silent",
                    centerX, centerY, radius, playerPosition);
        }
    }

    private void drawObject(Graphics2D g2, DimensionalPosition position, String type,
                            int centerX, int centerY, int radius, DimensionalPosition playerPosition) {
        if (position == null || playerPosition == null) return;

        // Calculate relative position
        double dx = position.getX() - playerPosition.getX();
        double dy = position.getY() - playerPosition.getY(); // Vertical axis
        double dz = position.getZ() - playerPosition.getZ();

        // Calculate horizontal distance
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        // Check if object is within radar range
        if (horizontalDistance > selectedRadarRangeKm) return;

        // Normalize to radar display
        double normalizedDistance = horizontalDistance / selectedRadarRangeKm;
        int displayRadius = (int) (normalizedDistance * radius);

        // Calculate angle
        double angle = Math.atan2(dz, dx);
        int x = centerX + (int) (Math.cos(angle) * displayRadius);

        // Calculate vertical position (Y-axis represents elevation)
        // Scale elevation to fit within radar display
        int maxElevationDisplay = radius / 3;
        int y = centerY - (int) (dy / selectedRadarRangeKm * maxElevationDisplay);

        // Keep within bounds
        y = Math.max(centerY - radius + 20, Math.min(centerY + radius - 20, y));

        // Determine object color and size
        Color color;
        int size;
        boolean drawConnector = true;

        switch (type) {
            case "Star":
                color = new Color(255, 225, 120);
                size = 12;
                drawConnector = false;
                break;
            case "Planet":
                color = new Color(110, 220, 255);
                size = 10;
                break;
            case "Moon":
                color = new Color(160, 240, 255);
                size = 7;
                break;
            case "Station":
                color = new Color(112, 255, 148);
                size = 8;
                break;
            case "Character":
                color = new Color(255, 120, 120);
                size = 6;
                break;
            case "Silent":
                color = new Color(180, 180, 180);
                size = 5;
                break;
            default:
                color = new Color(255, 120, 120);
                size = 6;
        }

        // Draw connector line to ground plane if needed
        if (drawConnector) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
            g2.drawLine(x, y, x, centerY);
        }

        // Draw object glow
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2.fill(new Ellipse2D.Double(x - size - 1, y - size - 1, (size + 1) * 2, (size + 1) * 2));

        // Draw object
        g2.setColor(color);
        g2.fillOval(x - size/2, y - size/2, size, size);

        // Draw object outline
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(x - size/2, y - size/2, size, size);

        // Draw elevation indicator
        if (Math.abs(dy) > 5) { // Only show if significant elevation difference
            int indicatorSize = Math.min(3, Math.max(1, Math.abs((int)dy) / 20));
            int indicatorY = dy > 0 ? y - size : y + size;
            g2.setColor(dy > 0 ? Color.GREEN : Color.RED);
            g2.fillOval(x - indicatorSize, indicatorY - indicatorSize, indicatorSize * 2, indicatorSize * 2);
        }
    }

    private void drawPlayerShip(Graphics2D g2, int centerX, int centerY) {
        // Draw ship as a triangle
        Polygon ship = new Polygon();
        ship.addPoint(centerX, centerY - 10);
        ship.addPoint(centerX - 8, centerY + 10);
        ship.addPoint(centerX + 8, centerY + 10);

        g2.setColor(PLAYER_COLOR);
        g2.fillPolygon(ship);
        g2.setColor(Color.WHITE);
        g2.drawPolygon(ship);

        // Draw center point
        g2.setColor(Color.YELLOW);
        g2.fillOval(centerX - 2, centerY - 2, 4, 4);
    }

    private void drawTelemetry(Graphics2D g2, int centerX, int centerY, int radius) {
        g2.setColor(new Color(94, 232, 133));
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));

        // Draw range indicators
        String[] ranges = {"20KM", "40KM", "60KM", "80KM", "100KM"};
        for (int i = 0; i < ranges.length && i < 5; i++) {
            int r = radius * (i + 1) / 5;
            g2.drawString(ranges[i], centerX + r - 25, centerY - r - 10);
        }

        // Draw elevation indicators
        String[] elevations = {"+50KM", "+25KM", "0KM", "-25KM", "-50KM"};
        for (int i = 0; i < elevations.length; i++) {
            int y = centerY - radius/3 * (i - 2);
            if (i == 2) continue; // Skip center
            g2.drawString(elevations[i], centerX - radius + 10, y + 4);
        }

        // Draw title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString("TACTICAL DISPLAY", centerX - 60, centerY + radius + 30);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString("RANGE: " + selectedRadarRangeKm + "KM", centerX + radius - 100, centerY + radius + 30);
    }
}