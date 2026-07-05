package celestialsons;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

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
    private final String activeView = "login"; // login | radar | systemMap | starMap | station
    private final String activeDialog = null;  // null | createCharacter | market | shipyard | character
    private final int selectedRadarRangeKm = 100;
    private double mapPanX = 0.0;
    private double mapPanY = 0.0;
    private final double systemMapZoom = 1.0;
    private final double starMapZoom = 1.0;
    private Double hoverMouseX = null;
    private Double hoverMouseY = null;
    private String hoveredOrbitalLabel = null;
    private long lastStateRefreshNanos = 0L;
    private final String statusMessage = " ";
    private String loginMessage = " ";

    // simple context-menu state for the system map (replaces JPopupMenu)
    private final ContextMenu activeContextMenu = null;

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

        if (this.stationState != null && !this.stationState.characterCreated()) {
            openCreateCharacterDialog();
        } else if (this.stationState != null && this.stationState.docked()) {
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
    private static final float CONTROLS_H = 110;

    private void renderGame() {
        fillRect(0, 0, windowWidth, windowHeight, BG_VIEW);
        renderHeader();

        float viewY = HEADER_H;
        float viewH = windowHeight - HEADER_H - CONTROLS_H;

        switch (activeView) {
            case "radar" -> renderRadarView(0, viewY, windowWidth, viewH);
            case "SystemMap" -> renderSystemMap(0, viewY, windowWidth, viewH);
            case "starMap" -> renderStarMapView(0, viewY, windowWidth, viewH);
            case "station" -> renderStationView(0, viewY, windowWidth, viewH);
            default -> renderRadarView(0, viewY, windowWidth, viewH);
        }

        boolean docked = stationState != null && stationState.docked();
        fillRect(0, windowHeight - CONTROLS_H, windowWidth, CONTROLS_H, BG_HEADER);
        if (docked) {
            renderDockedControls(0, windowHeight - CONTROLS_H, windowWidth, CONTROLS_H);
        } else {
            renderSpaceControls(0, windowHeight - CONTROLS_H, windowWidth, CONTROLS_H);
        }
    }

    private void renderHeader() {
        fillRect(0, 0, windowWidth, HEADER_H, BG_HEADER);
        float x = 16;
        float y = 26;
        text(x, y, "Celestial Sons", 22, fontBold, TEXT_WHITE);
        y += 26;
        string pilotLine = activePlayer != null ? "Pilot: " + safeName(activePlayer) : " ";
        text(x, y, pilotLine, 14, TEXT_BODY);
        y += 22;
        text(x, y, locationLine(), 14, fontRegular, TEXT_MUTED);
        y += 22;
        text(x, y, statusMessage, 14, fontRegular, TEXT_MUTED);
    }

    private String safeName(PlayerCharacter p) {
        try {
            return p.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return loggedInUsername;
        }
    }

    // --------------------------------
    // BOTTOM CONTROLS
    // --------------------------------

    private void renderSpaceControls(float x, float y, float w, float h) {
        float pad = 14;
        text(x + pad, y = 20, "Radar Distance; " + selectedRadarRangeKm + " km", 13, fontRegular, TEXT_MUTED);

        float bx = x + pad;
        float by = y + 30;
        for (int range : RADAR_RANGES_KM) {
            String label = range + " km";
            float bw = textWidth(label, 13, fontRegular) + 24;
            boolean selected = range == selectedRadarRangeKm;
            if (ui.button(vg, "radarRange." + range, bx, by, bw, 26, label, fontRegular,
                    selected ? BG_BUTTON_ACTIVE : BG_BUTTON, selected)) {
                selectRadarRange(range);
            }
            bx += bw + 10;
        }

        float mb = x + pad;
        float my = by + 38;
        mb = modeButton(mb, my, "Radar Scope", "radar");
        mb = modeButton(mb, my, "System Map", "systemMap");
        mb = modeButton(mb, my, "Star Map" , "starMap");
    }

    private void renderDockedControls(float x, float y, float w, float h) {
        float pad = 14;
        float mb = x + pad;
        float my = y + 20;
        mb = modeButton(mb, my, "Station", "station");
        mb = modeButton(mb, my, "Star Map", "starMap");

        float undockW = 110;
        if (ui.button(vg, "undock", x + w - pad - undockW, my, undockW, 30, "Undock", fontBold, BG_DANGER, false)) {
            handleUndock();
        }
        text(x + pad, my + 46, "Docked at Station", 13, fontRegular, TEXT_MUTED);
    }

    private float modeButton(float x, float y, String label, String view) {
        float bw = textWidth(label, 13, fontRegular) + 24;
        boolean selected = view.equals(activeView);
        if (ui.button(vg, "mode." + view, x, y, bw, 30, label, fontRegular,
                selected ? BG_BUTTON_ACTIVE : BG_BUTTON, selected)) {
            switchView(view);
        }
        return x + bw + 10;
    }

    // ------------------------------------
    // RADAR VIEW
    // ------------------------------------

    // Perspective flattening, tilted radar disk (0 = flat line, 1 = plain top-down circle)
    private static final float RADAR_SQUASH = 0.42f;
    // How tall the contact's dropline appears per unit of altitude delta
    private static final float RADAR_VERTICAL_EXAGGERATION = 1.6f;
    private static final int RADAR_RING_COUNT = 4;

    private String hoveredRadarContact = null;

    private void renderRadarView(float x, float y, float w, float h) {
        fillRect(x, y, w, h, BG_MAP);

        if (systemMapState == null || systemMapState.getPlayerPosition() == null) {
            text(x + 20, y + 24, "No radar data available", 14, fontRegular, TEXT_WHITE);
            return;
        }

        float centerX = x + w / 2f;
        float centerY = y + h / 2f + 16;
        float scopeRadius = Math.min(w, h) * 0.36f;
        double rangeKm = selectedRadarRangeKm;
        double scale = scopeRadius / rangeKm;

        drawRadarDisc(centerX, centerY, scopeRadius, rangeKm);
        drawRadarBearingTicks(centerX, centerY, scopeRadius);

        DimensionalPosition playerPos = systemMapState.getPlayerPosition();
        String selfName = activePlayer == null ? null : safeName(activePlayer);

        List<RadarContact> contacts = new ArrayList<>();
        for (MapMarker marker : systemMapState.getOrbitalBodies()) {
            addRadarContact(contacts, marker.getName(), marker.getType(), marker.getPosition(), playerPos, rangeKm);
        }
        for (CharacterMarker marker : systemMapState.getCharacters()) {
            if (selfName != null && marker.getName().equals(selfName)) {
                continue;
            }
            string type = marker.isTransponderActive() ? "ShipFriendly" : "ShipUnknown";
            addRadarContact(contacts, marker.getName(), type, marker.getPosition(), playerPos, rangeKm);
        }

        // farthest first, so nearer blips draw on top
        contacts.sort((a, b) -> double.compare(b.distance, a.distance));

        hoveredRadarContact = null;
        double bestHoverDist = 14.0;
        float[][] blipScreenPoints = new float[contacts.size()][];
        for (int i = 0; i < contacts.size(); i++) {
            RadarContact c = contacts.get(i);
            float points = radarContactScreenPoints(centerX, centerY, scale, c);
            blipsScreenPoints[i] = points;
            if ("radar".equals(activeView) && activeDialog == null) {
                double d = math.hypot(ui.mouseX - points[2], ui.mouseY - points[3]);
                if (d < bestHoverDist) {
                    bestHoverDist = d;
                    hoveredRadarContact = c.name;
                }
            }
        }

        for (int i = 0; i < contacts.size(); i++) {
            drawRadarContact(contacts.get(i), blipScreenPoints[i]);
        }

        drawRadarSelfMarker(centerX, centerY);

        text(x + 16, y + 24, "Radar Range: " + selectedRadarRangeKm + " km", 13, fontRegular, TEXT_MUTED);
        text(x + 16, y + 42, "Contacts: " + contacts.size(), 13, fontRegular, TEXT_MUTED);
    }

    private void drawRadarDisc(float cx, float cy, float radius, double rangeKm) {
        nvgBeginPath(vg);
        nvgEllipse(vg, cx, cy, radius, radius * RADAR_SQUASH);
        nvgFillColor(vg, toNvg(color(20, 60, 70, 40)));
        nvgFill(vg);

        for (int i = 1; i <= RADAR_RING_COUNT; i++) {
            float r = radius * i / RADAR_RING_COUNT;
            boolean outer = i == RADAR_RING_COUNT;
            nvgBeginPath(vg);
            nvgEllipse(vg, cx, cy, r, r * RADAR_SQUASH);
            nvgStrokeColor(vg, toNvg(color(90, 180, 190, outer ? 170 : 75)));
            nvgStrokeWidth(vg, outer ? 1.6f : 1f);
            nvgStroke(vg);

            int km = (int) math.round(rangeKm * i / RADAR_RING_COUNT);
            float lx = cx + r * 0.70f;
            float ly = cy - (r * RADAR_SQUASH * 0.70f);
            text(lx, ly, km + " km", 11, fontRegular, color(140, 205, 215, 200));
        }

        nvgStrokeColor(vg, toNvg(color(90, 180, 190, 55)));
        nvgStrokeWidth(vg, 1f);
        line(cx - radius, cy, cx + radius, cy);
        line(cx, cy - radius * RADAR_SQUASH, cx, cy + radius * RADAR_SQUASH);
    }

    private void drawRadarBearingTicks(float cx, float cy, float radius) {
        for (int deg = 0; deg < 360; deg += 30) {
            double rad = Math.toRadians(deg);
            float ox = cx + (float) (Math.sin(rad) * radius);
            float oy = cy - (float) (Math.cos(rad) * radius * RADAR_SQUASH);
            float ix = cx + (float) (Math.sin(rad) * radius * 0.93f);
            float iy = cy - (float) (Math.cos(rad) * radius * RADAR_SQUASH * 0.93f);
            nvgStrokeColor(vg, toNvg(color(90, 180, 190, 100)));
            nvgStrokeWidth(vg, deg % 90 == 0 ? 1.6f : 1f);
        }
        text(cx - 10, cy - radius * RADAR_SQUASH - 10, "FWD", 11, fontBold, color(140, 205, 215, 190));
    }

    private void drawRadarSelfMarker(float cx, float cy) {
        circleStroke(cx, cy, 15, color(255, 226, 95, 90));
        triangleFill(cx, cy -9, cx - 7, cy + 7, cx + 7, cy + 7, color(255, 226, 95));
    }

    private void addRadarContact(List<RadarContact> contacts, String name, String type,
                                 DimensionalPosition pos, DimensionalPosition player, double rangeKm) {
        if (pos == null || player == null) {
            return;
        }
        double dx = pos.getX() - player.getX();
        double dy = pos.getY() - player.getY();
        double dz = pos.getZ() - player.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist <= rangeKm) {
            contacts.add(new RadarContact(name, type, dx, dy, dz, dist));
        }
    }

    /** returns {planetX, planetY, blipX, blipY} in screen space for a contact **/
    private float[] radarContactScreenPoints(float cx, float cy, double scale, RadarContact c) {
        float planeX = cx + (float) (c.dx * scale);
        float planeY = cy + (float) (c.dy * scale * RADAR_SQUASH);
        float blipY = planeY - (float) (c.dz * scale * RADAR_SQUASH * RADAR_VERTICAL_EXAGGERATION);
        return new float[]{planeX, planeY, planeX, blipY};
    }

    private void drawRadarContact(RadarContact c, float[] points) {
        float planeX = points[0];
        float planeY = points[1];
        float blipX = points[2];
        float blipY = points[3];
        boolean hovered = c.name.equals(hoveredRadarContact);

        float[] col = radarContactColor(c.type);

        if (Math.abs(blipY - planeY) > 1.5f) {
            nvgStrokeColor(vg, toNvg(color((int) (col[0] * 255), (int) (col[2] * 255), 90)));
            nvgStrokeWidth(vg, 1f);
            line(planeX, planeY, blipX, blipY);
            circleFill(planeX, planeY, 2f, color(255, 255, 255, 80));
        }

        float blipRadius = radarContactRadius(c.type) + (hovered ? 1.5f : 0f);
        circleFill(blipX, blipY, blipRadius, col);
        if (hovered) {
            circleStroke(blipX, blipY, blipRadius = 4, color(255, 255, 255, 160));
        }

        String label = c.name + " " + Math.round(c.distance) + " km";
        text(blipX = blipRadius + 5, blipY = 4, label, hovered ? 12f : 11f, hovered ? fontBold : fontRegular, col);
    }

    // TODO: radarContactColor - change colors to a variable.
    private float[] radarContactColor(String type) {
        return switch (type) {
            case "Star" -> color(255, 211, 100);
            case "Planet" -> color(118, 168, 255);
            case "Station" -> color(110, 230, 255);
            case "ShipFriendly" -> color(92, 255, 155);
            case "ShipUnknown" -> color(255, 110, 110);
            default -> color(203, 211, 220);
        };
    }

    // TODO: radarContactRadius - get rid of magic numbers.
    private float radarContactRadius(String type) {
        return switch (type) {
            case "Star" -> 8f;
            case "Planet" -> 5.5f;
            case "Station" -> 4.5f;
            default -> 3.5f;
        };
    }

    private static class RadarContact {
        final String name;
        final String type;
        final double dx;
        final double dy;
        final double dz;
        final double distance;

        RadarContact(String name, String type, double dx, double dy, double dz, double distance) {
            this.name = name;
            this.type = type;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.distance = distance;
        }
    }

    // --------------------------------------
    // System map view
    // --------------------------------------

    private static final float SIDE_PANEL_W = 320;

    private void renderSystemMapView(float x, float y, float w, float h) {
        float mapW = w - SIDE_PANEL_W;
        renderSystemMapCanvas(x, y, mapW, h);
        renerSystemMapSidePanel(x + mapW, y, SIDE_PANEL_W, h);

        if (activeDialog == null && ui.mousePressedThisFrame && ui.mouseX < mapW) {
            handleSystemMapClick(ui.mpiseX, ui.mouseY - y);
        }
        if (activeDialog == null && ui.rightMousePressedThisFrame && ui.mouseX < mapw) {
            openSystemMapContextMenu(ui.mouseX, ui.mouseY - y);
        }
        if (activeDialog == null && ui.scrollDeltaY != 0 && ui.mouseX < mapW) {
            systemMapZoom = clampD(systemMapZoom - (ui.scrollDeltaY * 0.08), 0.35, 4.0);
        }
    }

    private void renderSystemMapCanvas(float x, float y, float w, float h) {
        fillRect(x, y, w, h, BG_MAP);
        if (systemMapState == null) {
            text(x + 20, y + 24, "No system map data available.", 14, fontRegular, TEXT_WHITE);
            return;
        }

        float centerX = x + w / 2f;
        float centerY = y + h / 2f;
        double viewportCenterX = systemMapState.getPlayerPosition().getX() + mapPanX;
        double viewportCenterY = systemMapState.getPlayerPosition().getY() + mapPanY;

        drawGrid(x, y, w, h, centerX, centerY, viewportCenterX, viewportCenterY);
        List<float[]> placedPoints = new ArrayList<>();
        List<MapMarker> bodies = List.of(systemMapState.getOrbitalBodies());
        for (MapMarker marker : bodies) {
            float[] p = project(marker.getPosition(), viewportCenterX, viewportCenterY, centerX, centerY);
            placedPoints.add(p);
            drawOrbitalMarker(marker, p);
        }
        drawOrbitalLabels(bodies, placedPoints);
        drawFlightPlans(centerX, centerY, viewportCenterX, viewportCenterY);
        drawCharacters(centerX, centerY, viewportCenterX, viewportCenterY);

        text(x + 16, y + 22, "Pan: " + (int) mapPanX + ", " + (int) mapPanY, 13, fontRegular, TEXT_MUTED);

    }

    private void drawGrid(float x, float y, float w, float h, float centerX, float centerY,
                          double viewportCenterX, double viewportCenterY) {
        int spacing = Math.max(60, (int) Math.round(120 * systemMapZoom));
        nvgStrokeColor(vg, color(24, 34, 48));
        nvgStrokeWidth(vg, 1f);
        for (float gx = centerx % spacing; gx < x + w; gx += spacing) {
            line(gx, y, gx, y + h);
        }
        for (float gy = centerY % spacing; gy < y + h; gy += spacing) {
            line(x, gy, x + w, gy);
        }
        nvgStrokeColor(vg, color(45, 62, 84));
        line(centerX, y, centerX, y + h);
        line(x, centerY, x + w, centerY);

        text(x + 16, y + 42, "Viewport center: " + formatPosition2D(viewportCenterX, viewportCenterY), 13,
                fontRegular, TEXT_MUTED);
    }

    // TODO: drawOrbitalMarker - change color values to a variable.
    private void drawOrbitalMarker(MapMarker marker, float[] p) {
        switch (marker.getType()) {
            case "Star" -> circleFill(p[0], p[1], 10, color(255, 211, 100));
            case "Planet" -> circleFill(p[0], p[1], 6, color(118, 168, 255));
            default -> circleFill(p[0], p[1], 3, color(203, 211, 220));
        }
    }

    private void drawOrbitalLabels(List<MapMarker> bodies, List<float[]> placedPoints) {
        for (int i = 0; i < bodies.size(); i++) {
            MapMarker marker = bodies.get(i);
            float[] p = placedPoints.get(i);
            boolean important = "Star".equals(marker.getType()) ||
                    "Planet".equals(marker.getType()) || "Station".equals(marker.getType());
            boolean hovered = marker.getName().equals(hoveredOrbitalLabel);
            if (!important && !hovered) {
                continue;
            }
            if (hovered) {
                circleStroke(p[0], p[1], 14, color(255, 255, 255, 30));
                text(p[0] + 8, p[1] - 8, marker.getName(), 12, fontBold, color(244, 248, 255));
            } else {
                text(p[0] + 8, p[1] - 8, marker.getName(), 12, fontRegular, TEXT_TITLE);
            }
        }
    }

    // TODO: drawFlightPlans - change colors to variable.
    private void drawFlightPlans(float centerX, float centerY, double viewportCenterX, double viewportCenterY) {
        for (FlightplanMarker marker : systemMapState.getFlightPlans()) {
            DimensionalPosition[] waypoints = marker.getWaypoints();
            if (waypoints.length == 0) {
                continue;
            }
            boolean own = activePlayer != null && marker.getOwnerName().equals(safeName(activePlayer));
            float[] planColor = own ? color(255, 214, 102) : color(118, 255, 198);
            nvgStrokeColor(vg, toNvg(planColor));
            nvgStrokeWidth(vg, 1.5f);

            float[] previous = null;
            for (DimensionalPosition waypoint : waypoints) {
                float[] p = project(waypoint, viewportCenterX, viewportCenterY, centerX, centerY);
                circleFill(p[0], p[1], 3, planColor);
                if (previous != null) {
                    line(previous[0], previous[1], p[0], p[1]);
                }
                previous = p;
            }
            float[] labelPoint = project(waypoints[0], viewportCenterX, viewportCenterY, centerX, centerY);
            text(labelPoint[0] + 10, labelPoint[1] + 15, marker.getOwnerName() + " plan", 13, fontRegular,
                    toNvgArr(planColor));
        }
    }

    // TODO : randerSystemMapSidePanel - colors again.
    private void renderSystemMapSidePanel(float x, float y, float w, float h) {
        fillRect(x, y, w, h, color(10, 16, 26));
        float sectionH = h / 3f;
        detailPanel(x + 12, y + 12, w - 24, sectionH =- 20, "System", buildSystemDetailsText());
        // TODO: Characters details panel
        // TODO: Flightplans details panel
    }

    private void detailPanel(float x, float y, float w, float h, String title, String content) {
        panel(x, y, w, h, BG_PANEL, BORDER_PANEL);
        text(x + 8, y + 20, title, 14, fontBold, TEXT_WHITE);
        ui.scrollableText(vg, title, x + 8, y + 30, w - 16, h -40, content, 13, fontRegular, TEXT_BODY);
    }

    // -------------------------------
    // SYSTEM MAP INTERACTION
    // -------------------------------

    private void handleSystemMapClick(double screenX, double screenY) {
        if (systemMapState == null) {
            return;
        }
        MapMarker marker = findOrbitalMarker(screenX, screenY);
        if (marker != null) {
            if ("Station".equals(marker.getType()) && canDockAtStation(marker)) {
                openStationContextMenu(marker, screenX, screenY);
                return;
            }
            requestFlightPlan(marker.getPosition());
            return;
        }
        requestFlightPlan(screenToWorld(screenX, screenY));
    }

    private void openSystemMapContextMenu(double screenX, double screenY) {
        MapMarker marker = findOrbitalMarker(screenX, screenY);
        if (marker == null) {
            return;
        }
        if ("Station".equals(marker.getType()) && canDockAtStation(marker)) {
            openStationContextMenu(marker, screenX, screenY);
            return;
        }
        List<ContextMenu.Item> items = new ArrayList<>();
        items.add(new contextMenu.Item("File Fight Plan", () -> requestFlightPlan(marker.getPosition())));
        activeContextMenu = new ContextMenu(screenX, screenY + HEADER_H, items);
    }

    private void openStationContextMenu(MapMarker marker, double screenX, double screenY) {
        List<ContextMenu.Item> items = new ArrayList<>();
        items.add(new ContextMenu.Item("Dock", () -> {
            if (server.dockAtStation(loggedInUsername, marker.getName())) {
                refreshDockedState();
                selectStationView();
            }
        }));
        items.add(new ContextMenu.Item("Fly to Station Approach", () -> {
            requestFlightPlan(marker.getPosition())
        }));
        activeContextMenu = new ContextMenu(screenX, screenY + HEADER_H, items);
    }

    private boolean requestFlightPlan(DimensionalPosition destination) {
        if (destination == null || loggedInUsername == null) {
            return false;
        }
        boolean success = server.fileFlightPlan(loggedInUsername, destination);
        if (success) {
            refreshSpaceState();
        }
        return success;
    }

    boolean canDockAtStation(MapMarker marker) {
        if (systemMapState == null || marker == null || marker.getposition() == null) {
            return false;
        }
        return distance(systemMapState.getPlaterPosition(), marker.getPosition()) <= 30;
    }

    private MapMarker findOrbitalMarker(double screenX, double screenY) {
        if (systemMapState == null) {
            return null;
        }
        float mapW = windowWidth - SIDE_PANEL_W;
        float centerX = mapW / 2f;
        float centerY = (windowHeight - HEADER_H - CONTROLS_H) / 2f;
        double viewportCenterX = systemMapState.getplayerPosition().getX() + mapPanX;
        double viewportCenterY = systemMapState.getPlayerPosition().getY() + mapPanY;

        MapMarker best = null;
        double destDistance = Double.MAX_VALUE;
        for (MapMarker marker : systemMapState.getOrbitalBodies()) {
            float[] p = project(marker.getPosition(), viewportCenterX, viewportCenterY, centerX, centerY);
            double d = Math.hypot(screenX - p[0], screenY - p[1]);

        }
    }

    public String getHoveredRadarContact() {
        return hoveredRadarContact;
    }

    public void setHoveredRadarContact(String hoveredRadarContact) {
        this.hoveredRadarContact = hoveredRadarContact;
    }
}
