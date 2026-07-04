package celestialsons;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import celestialsons.orbitalbodies.Star;
import celestialsons.market.Contract;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GameClient {

    private static final int[] RADAR_RANGES_KM = {1, 10, 50, 100, 200, 1000};
    private static final double SYSTEM_MAP_SCALE = 1.0;
    private static final double SYSTEM_MAP_PAN_STEP = 120.0;
    private static final int WINDOW_W = 1280;
    private static final int WINDOW_H = 920;
    private static final long STATE_REFRESH_INTERVAL_NANOS = 250_000_000L;

    private static final String FONT_REGULAR_PATH = "assets/fonts/Roboto-Regular.ttf";
    private static final String FONT_BOLD_PATH = "assets/fonts/Roboto-Bold.ttf";

    // Palette
    private static final float[] BG_ROOT = rgb(11, 17, 28);
    private static final float[] BG_FORM = rgb(19, 27, 42);
    private static final float[] BG_HEADER = rgb(12, 18, 29);
    private static final float[] BG_VIEW = rgb(9, 13, 22);
    private static final float[] BG_MAP = rgb(6, 10, 16);
    private static final float[] BG_PANEL = rgb(16, 24, 37);
    private static final float[] BG_FIELD = rgb(20, 28, 42);
    private static final float[] BG_BUTTON = rgb(28, 38, 58);
    private static final float[] BG_BUTTON_ACTIVE = rgb(52, 74, 112);
    private static final float[] BG_DANGER = rgb(140, 16, 16);
    private static final float[] BORDER_SOFT = rgb(61, 78, 107);
    private static final float[] BORDER_PANEL = rgb(49, 64, 88);
    private static final float[] TEXT_WHITE = rgb(255, 255, 255);
    private static final float[] TEXT_TITLE = rgb(235, 238, 244);
    private static final float[] TEXT_MUTED = rgb(172, 184, 204);
    private static final float[] TEXT_BODY = rgb(215, 223, 235);
    private static final float[] TEXT_WARNING = rgb(246, 203, 88);

    private final GameServerConnection server;
    private final Ui ui = new Ui();

    private long window;
    private long vg;
    private int fontRegular;
    private int fontBold;
    private int windowWidth = WINDOW_W;
    private int windowHeight = WINDOW_H;

    private PlayerCharacter activePlayer;
    private String loggedInUsername;
    private SystemMapState systemMapState;
    private StationState stationState;
    private String activeView = "login"; // login | radar | systemMap | starMap | station
    private String activeDialog = null;  // null | createCharacter | market | shipyard | character
    private int selectedRadarRangeKm = 100;
    private double mapPanX = 0.0;
    private double mapPanY = 0.0;
    private double systemMapZoom = 1.0;
    private double starMapZoom = 1.0;
    private Double hoverMouseX = null;
    private Double hoverMouseY = null;
    private String hoveredOrbitalLabel = null;
    private long lastStateRefreshNanos = 0L;
    private String statusMessage = " ";
    private String loginMessage = " ";

    // simple context-menu state for the system map (replaces JPopupMenu)
    private ContextMenu activeContextMenu = null;

    public GameClient(GameServerConnection server) {
        this.server = server;
    }

    public void show() {
        init();
        loop();
        cleanup();
    }

    // -------------------------
    //  BOOTSTRAP
    // -------------------------

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        window = glfwCreateWindow(windowWidth, windowHeight, "Celestial Sons", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW Window.");
        }

        setupCallbacks();

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidmode != null) {
                glfwSetWindowPos(window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2);
            }
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);

        vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (vg == NULL) {
            throw new RuntimeException("Could not initialize NanoVG.");
        }
        fontRegular = nvgCreateFont(vg, "regular", FONT_REGULAR_PATH);
        fontBold = nvgCreateFont(vg, "bold", FONT_BOLD_PATH);
        if (fontRegular == -1 || fontBold == -1) {
            System.err.println("Warning: failed to load one or more fonts. Update FONT_REGULAR_PATH/ FONT_BOLD_PATH.");
        }
    }

    private void setupCallbacks() {
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            windowWidth = w;
            windowHeight = h;
        });

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            ui.mouseX = xpos;
            ui.mouseY = ypos;
            if ("systemMap".equals(activeView) && activeDialog == null) {
                hoverMouseX = xpos;
                hoverMouseY = ypos;
                hoveredOrbitalLabel = findHoveredOrbitalLabel(xpos, ypos);
            }
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    ui.mouseDown = true;
                    ui.mousePressedThisFrame = true;
                } else if (acion == GLFW_RELEASE) {
                    ui.mouseDown = false;
                }
            } else if (buton == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
                ui.rightMouseButtonPressedThisFrame = true;
            }
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            ui.scrollDeltaY += yoffset;
        });

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action != GLFW_PRESS && action != GLFW_REPEAT) {
               return;
            }
            if (key == GLFW_KEY_BACKSPACE) {
                ui.backspaceRequested = true;
            } else if (key == GLFW_KEY_ENTER) {
                ui.enterRequested = true;
            } else if (action == GLFW_PRESS && "systemMap".equals(activeView) && activeDialog == null) {
                if (key == GLFW_KEY_LEFT) {
                    mapPanX -= SYSTEM_MAP_PAN_STEP;
                    refreshSpaceState();
                } else if (key == GLFW_KEY_RIGHT) {
                    mapPanX += SYSTEM_MAP_PAN_STEP;
                    refreshSpaceState();
                } else if (key == GLFW_KEY_UP) {
                    mapPanY -= SYSTEM_MAP_PAN_STEP;
                    refreshSpaceState();
                } else if (key == GLFW_KEY_DOWN) {
                    mapPanY = SYSTEM_MAP_PAN_STEP;
                    refreshSpaceState();
                }
            }
        });

        glfwSetCharCallback(window, (win, codepoint) -> ui.charQueue.add((char) codepoint));
    }

    // -----------------------
    //  MAIN LOOP
    // -----------------------

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            long now = System.nanoTime();
            if (now - lastStateRefreshNanos >= STATE_REFRESH_INTERVAL_NANOS) {
                refreshActiveState();
                lastStateRefreshNanos = now;
            }
        }

        glViewport(0, 0, windowWidth, windowHeight);
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(window, pWidth, pHeight);
            float pxRatio = pWidth.get(0) > 0 ? (float) pWidth.get(0) / windowWidth : 1f;

            nvgBeginFrame(vg, windowWidth, windowHeight, pxRatio);
            render();
            nvgEndFrame(vg);
        }

        ui.endFrame();
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    private void cleanup() {
        nnvgDelete(vg);
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    // -----------------------------
    // TOP-LEVEL RENDER DISPATCH
    // -----------------------------

    private void render() {
        if ("login".equals(activeView)) {
            renderLogin();
            return;
        }
        renderGame();
        if (activeDialog != null) {
            renderDialogOverlay();
        }
        if (activeContextMenu != null) {
            renderContextMenu();
        }
    }

    // ----------------------------
    // LOGIN SCREEN
    // ----------------------------

    private final StringBuilder usernameBuffer = new StringBuilder();
    private final StringBuilder passwordBuffer = new StringBuilder();

    private void renderLogin() {
        fillRect(0, 0, windowWidth, windowHeight, BG_ROOT);

        float formW = 420;
        float formH = 340;
        float formX = (windowWidth - formW) / 2f;
        float formY = (windowHeight - formH) / 2f;

        panel(formX, formY, formW, formH, BG_FORM, BORDER_SOFT);

        float pad = 24;
        float x = formx + pad;
        float y = formY + pad + 10;

        text(x, y, "Celestial Sons", 28, fontBold, TEXT_TITLE);
        y += 34;
        text(x, y, "Login", 15, fontRegular, TEXT_MUTED);
        y += 26;
        text(x, y, "Server: " + server.getServerName(), 14, fontRegular, TEXT_MUTED);
        y += 34;

        text(x, y, "Username", 14, fontRegular, TEXT_WHITE);
        ui.textField(vg, "login.username", x + 120, y - 16, formW - pad * 2 - 120, 26, usernameBuffer, false, fontRegular);

        y += 38;

        text(x, y, "Password", 14, fontRegular, TEXT_WHITE);
        ui.textField(vg, "login.password", x + 120, y - 16, formW - pad * 2 - 120, 26, passwordBuffer, true, fontRegular);

        y += 38;

        text(x, y, loginMessage, 13, fontRegular, TEXT_WARNING);
        y += 26;

        boolean submit = ui.button(vg, "login.submit", x, y, 120, 34, "Login", fontBold, BG_BUTTON, false);
        if (submit || (uieterConsumedThisWidget("login.password") && ui.enterRequested)) {
            attemptLogin();
        }
    }

        private void attemptLogin() {
            String username = usernameBuffer.toString().trim();
            char[] password = passwordBuffer.toString().toCharArray();

            LoginResult result = this.server.loginOrCreateUser(username, password);
            java.util.Arrays.fill(password, '\0');
            passwordBuffer.setLength(0);

            if (!result.isSuccess()) {
                this.loginMessage = result.getMessage();
                return;
            }

            this.activePlayer = result.getPlayerCharacter();
            this.loggedInUsername = username;
            this.loginMessage = result.getMessage();
            this.stationState = this.server.getStationState(this.loggedInUsername);

            if (this.stationState != null && !this.stationState.isCharacterCreated()) {
                openCreateCharacterDialog();
            } else if (this.stationState != null && this.stationState.isDocked()) {
                refreshDockedState();
                selectStationView();
            } else {
                refreshSpaceState();
                selectRadarView();
            }
        }

        // --------------------------------------
        // GAME SHELL : HEADER + VIEW + BOTTOM CONTROLS
        // --------------------------------------

        private static final float HEADER_H = 110;


    }


}
