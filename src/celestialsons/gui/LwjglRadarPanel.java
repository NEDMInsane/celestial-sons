package celestialsons.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import celestialsons.DimensionalPosition;
import celestialsons.MapMarker;
import celestialsons.CharacterMarker;
import celestialsons.PlayerCharacter;
import celestialsons.lwjgl.Radar3D;

public class LwjglRadarPanel extends JPanel {
    private long window;
    private Radar3D radar3D;
    private PlayerCharacter player;
    private List<MapMarker> orbitalBodies;
    private List<CharacterMarker> characters;
    private int selectedRadarRangeKm = 100;
    private boolean initialized = false;
    private boolean initializationFailed = false;
    private long lastFrameTime = 0;

    public LwjglRadarPanel() {
        setLayout(new BorderLayout());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (initialized && window != 0) {
                    // Update framebuffer size when component is resized
                    GLFW.glfwSetWindowSize(window, getWidth(), getHeight());
                }
            }
        });
    }

    public void initialize() {
        if (initialized || initializationFailed) return;

        try {
            // Initialize GLFW
            if (!GLFW.glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            // Configure GLFW
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

            // Create the window
            window = GLFW.glfwCreateWindow(Math.max(getWidth(), 100), Math.max(getHeight(), 100), "3D Radar", MemoryUtil.NULL, MemoryUtil.NULL);
            if (window == 0) {
                throw new RuntimeException("Failed to create the GLFW window");
            }

            // Set window position to be off-screen but visible for rendering
            GLFW.glfwSetWindowPos(window, -2000, -2000);

            // Make the OpenGL context current
            GLFW.glfwMakeContextCurrent(window);

            // Enable v-sync
            GLFW.glfwSwapInterval(1);

            // Make the window "visible" internally but not shown on screen
            GLFW.glfwShowWindow(window);

            // Initialize OpenGL
            GL.createCapabilities();

            // Create the 3D radar
            radar3D = new Radar3D(window);

            initialized = true;
            lastFrameTime = System.nanoTime();
        } catch (Exception e) {
            e.printStackTrace();
            initializationFailed = true;
            JOptionPane.showMessageDialog(this, "Failed to initialize 3D radar: " + e.getMessage());
        }
    }

    public void updateData(PlayerCharacter player, List<MapMarker> orbitalBodies,
                           List<CharacterMarker> characters, int selectedRadarRangeKm) {
        this.player = player;
        this.orbitalBodies = orbitalBodies;
        this.characters = characters;
        this.selectedRadarRangeKm = selectedRadarRangeKm;

        if (radar3D != null) {
            radar3D.updateData(player, orbitalBodies, characters, selectedRadarRangeKm);
        }
    }

    public void render() {
        if (!initialized || window == 0) return;

        // Calculate delta time
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = currentTime;

        // Render the 3D radar
        if (radar3D != null) {
            radar3D.render(deltaTime);
        }

        // Swap buffers
        GLFW.glfwSwapBuffers(window);

        // Poll for window events
        GLFW.glfwPollEvents();
    }

    public void cleanup() {
        if (radar3D != null) {
            radar3D.cleanup();
        }

        if (window != 0) {
            GLFW.glfwDestroyWindow(window);
        }

        GLFW.glfwTerminate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!initialized && !initializationFailed) {
            initialize();
        }

        if (initialized) {
            render();

            // Draw a placeholder message on successful initialization
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.GREEN);
            g.drawString("3D Radar Active - LWJGL Rendering", 10, 20);
            g.drawString("Objects: " + (orbitalBodies != null ? orbitalBodies.size() : 0) +
                    " + " + (characters != null ? characters.size() : 0), 10, 40);
        } else if (initializationFailed) {
            // Draw a placeholder when initialization fails
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.drawString("3D Radar - LWJGL initialization failed", 10, 20);
        } else {
            // Draw initializing message
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.YELLOW);
            g.drawString("3D Radar - Initializing...", 10, 20);
        }
    }
}