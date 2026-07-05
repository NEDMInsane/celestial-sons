package celestialsons.lwjgl;

import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;

import celestialsons.DimensionalPosition;
import celestialsons.MapMarker;
import celestialsons.CharacterMarker;
import celestialsons.PlayerCharacter;

public class Radar3D {
    // Vertex shader source code
    private static final String VERTEX_SHADER_SOURCE =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "layout (location = 1) in vec3 aColor;\n" +
                    "uniform mat4 model;\n" +
                    "uniform mat4 view;\n" +
                    "uniform mat4 projection;\n" +
                    "out vec3 ourColor;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                    "    ourColor = aColor;\n" +
                    "}\n";

    // Fragment shader source code
    private static final String FRAGMENT_SHADER_SOURCE =
            "#version 330 core\n" +
                    "in vec3 ourColor;\n" +
                    "out vec4 FragColor;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    FragColor = vec4(ourColor, 1.0f);\n" +
                    "}\n";

    // Radar configuration
    private static final float RADAR_RANGE_KM = 100.0f;
    private static final int GRID_SEGMENTS = 16;
    private static final float GRID_HEIGHT_SCALE = 0.5f;

    private final long window;
    private int shaderProgram;
    private int vaoGrid, vboGrid;
    private int vaoObjects, vboObjects;
    private int objectCount = 0;

    private PlayerCharacter player;
    private List<MapMarker> orbitalBodies = new ArrayList<>();
    private List<CharacterMarker> characters = new ArrayList<>();
    private int selectedRadarRangeKm = 100;

    // Camera parameters
    private float cameraAngleX = 0.0f;
    private final float cameraAngleY = 0.0f;
    private final float cameraDistance = 20.0f;
    private final float cameraHeight = 5.0f;

    public Radar3D(long window) {
        this.window = window;
        init();
    }

    public void init() {
        // Create shaders
        int vertexShader = compileShader(VERTEX_SHADER_SOURCE, GL_VERTEX_SHADER);
        int fragmentShader = compileShader(FRAGMENT_SHADER_SOURCE, GL_FRAGMENT_SHADER);

        // Create shader program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        // Check for linking errors
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == 0) {
            System.err.println("Shader program linking failed: " + glGetProgramInfoLog(shaderProgram));
        }

        // Delete shaders as they're linked into our program now and no longer necessary
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        // Generate grid VAO and VBO
        vaoGrid = glGenVertexArrays();
        vboGrid = glGenBuffers();

        // Generate objects VAO and VBO
        vaoObjects = glGenVertexArrays();
        vboObjects = glGenBuffers();

        // Create grid
        createGrid();
    }

    private int compileShader(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        // Check for compile errors
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.err.println("Shader compilation failed: " + glGetShaderInfoLog(shader));
        }

        return shader;
    }

    private void createGrid() {
        try (MemoryStack stack = stackPush()) {
            // Create grid vertices (cylindrical grid)
            List<Float> gridVertices = new ArrayList<>();

            // Vertical lines (range indicators)
            for (int i = 0; i <= 5; i++) {
                float radius = (RADAR_RANGE_KM * i) / 5.0f;
                for (int j = 0; j < GRID_SEGMENTS; j++) {
                    float angle1 = (float) (2 * Math.PI * j / GRID_SEGMENTS);
                    float angle2 = (float) (2 * Math.PI * (j + 1) / GRID_SEGMENTS);

                    // Bottom point
                    gridVertices.add((float) (radius * Math.cos(angle1)));
                    gridVertices.add(-RADAR_RANGE_KM * GRID_HEIGHT_SCALE);
                    gridVertices.add((float) (radius * Math.sin(angle1)));
                    gridVertices.add(0.0f); // Color (r)
                    gridVertices.add(0.8f); // Color (g)
                    gridVertices.add(0.0f); // Color (b)

                    // Top point
                    gridVertices.add((float) (radius * Math.cos(angle2)));
                    gridVertices.add(-RADAR_RANGE_KM * GRID_HEIGHT_SCALE);
                    gridVertices.add((float) (radius * Math.sin(angle2)));
                    gridVertices.add(0.0f); // Color (r)
                    gridVertices.add(0.8f); // Color (g)
                    gridVertices.add(0.0f); // Color (b)
                }
            }

            // Horizontal lines (elevation indicators)
            for (int i = 0; i <= 8; i++) {
                float height = (RADAR_RANGE_KM * GRID_HEIGHT_SCALE * 2 * i / 8.0f) - (RADAR_RANGE_KM * GRID_HEIGHT_SCALE);
                for (int j = 0; j < GRID_SEGMENTS; j++) {
                    float angle1 = (float) (2 * Math.PI * j / GRID_SEGMENTS);
                    float angle2 = (float) (2 * Math.PI * (j + 1) / GRID_SEGMENTS);
                    float radius = RADAR_RANGE_KM;

                    // Point 1
                    gridVertices.add((float) (radius * Math.cos(angle1)));
                    gridVertices.add(height);
                    gridVertices.add((float) (radius * Math.sin(angle1)));
                    gridVertices.add(0.0f); // Color (r)
                    gridVertices.add(0.8f); // Color (g)
                    gridVertices.add(0.0f); // Color (b)

                    // Point 2
                    gridVertices.add((float) (radius * Math.cos(angle2)));
                    gridVertices.add(height);
                    gridVertices.add((float) (radius * Math.sin(angle2)));
                    gridVertices.add(0.0f); // Color (r)
                    gridVertices.add(0.8f); // Color (g)
                    gridVertices.add(0.0f); // Color (b)
                }
            }

            // Center point
            gridVertices.add(0.0f);
            gridVertices.add(0.0f);
            gridVertices.add(0.0f);
            gridVertices.add(1.0f); // Color (r)
            gridVertices.add(1.0f); // Color (g)
            gridVertices.add(0.0f); // Color (b)

            gridVertices.add(1.0f);
            gridVertices.add(0.0f);
            gridVertices.add(0.0f);
            gridVertices.add(1.0f); // Color (r)
            gridVertices.add(1.0f); // Color (g)
            gridVertices.add(0.0f); // Color (b)

            // Convert to array
            float[] vertices = new float[gridVertices.size()];
            for (int i = 0; i < gridVertices.size(); i++) {
                vertices[i] = gridVertices.get(i);
            }

            // Bind grid VAO
            glBindVertexArray(vaoGrid);

            // Bind grid VBO and upload data
            glBindBuffer(GL_ARRAY_BUFFER, vboGrid);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

            // Position attribute
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            // Color attribute
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            // Unbind
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }

    private void updateObjectData() {
        if (player == null) return;

        try (MemoryStack stack = stackPush()) {
            // Create object vertices
            List<Float> objectVertices = new ArrayList<>();
            DimensionalPosition playerPos = getPlayerPosition();

            // Add the player ship
            objectVertices.add(0.0f); // x
            objectVertices.add(0.0f); // y
            objectVertices.add(0.0f); // z
            objectVertices.add(1.0f); // r
            objectVertices.add(1.0f); // g
            objectVertices.add(0.0f); // b
            objectCount = 1;

            // Add orbital bodies
            for (MapMarker marker : orbitalBodies) {
                if (marker.position() != null) {
                    DimensionalPosition pos = marker.position();
                    float dx = (float) (pos.getX() - playerPos.getX());
                    float dy = (float) (pos.getY() - playerPos.getY());
                    float dz = (float) (pos.getZ() - playerPos.getZ());

                    // Check if object is within radar range
                    float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (distance <= selectedRadarRangeKm) {
                        objectVertices.add(dx);
                        objectVertices.add(dy);
                        objectVertices.add(dz);

                        // Set color based on object type
                        switch (marker.type()) {
                            case "Star":
                                objectVertices.add(1.0f); // r
                                objectVertices.add(0.9f); // g
                                objectVertices.add(0.0f); // b
                                break;
                            case "Planet":
                                objectVertices.add(0.0f); // r
                                objectVertices.add(0.5f); // g
                                objectVertices.add(1.0f); // b
                                break;
                            case "Moon":
                                objectVertices.add(0.5f); // r
                                objectVertices.add(0.8f); // g
                                objectVertices.add(1.0f); // b
                                break;
                            case "Station":
                                objectVertices.add(0.0f); // r
                                objectVertices.add(1.0f); // g
                                objectVertices.add(0.5f); // b
                                break;
                            default:
                                objectVertices.add(1.0f); // r
                                objectVertices.add(0.0f); // g
                                objectVertices.add(0.0f); // b
                        }
                        objectCount++;
                    }
                }
            }

            // Add characters/ships
            for (CharacterMarker marker : characters) {
                if (marker.position() != null) {
                    DimensionalPosition pos = marker.position();
                    float dx = (float) (pos.getX() - playerPos.getX());
                    float dy = (float) (pos.getY() - playerPos.getY());
                    float dz = (float) (pos.getZ() - playerPos.getZ());

                    // Check if object is within radar range
                    float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (distance <= selectedRadarRangeKm) {
                        objectVertices.add(dx);
                        objectVertices.add(dy);
                        objectVertices.add(dz);

                        // Set color based on transponder status
                        if (marker.transponderActive()) {
                            objectVertices.add(1.0f); // r
                            objectVertices.add(0.0f); // g
                            objectVertices.add(0.0f); // b
                        } else {
                            objectVertices.add(0.5f); // r
                            objectVertices.add(0.5f); // g
                            objectVertices.add(0.5f); // b
                        }
                        objectCount++;
                    }
                }
            }

            // Convert to array
            float[] vertices = new float[objectVertices.size()];
            for (int i = 0; i < objectVertices.size(); i++) {
                vertices[i] = objectVertices.get(i);
            }

            // Bind objects VAO
            glBindVertexArray(vaoObjects);

            // Bind objects VBO and upload data
            glBindBuffer(GL_ARRAY_BUFFER, vboObjects);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

            // Position attribute
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            // Color attribute
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            // Unbind
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }

    private DimensionalPosition getPlayerPosition() {
        if (player != null && player.getCurrentLocation() != null) {
            return player.getCurrentLocation();
        }
        return new DimensionalPosition(0.0, 0.0, 0.0);
    }

    public void updateData(PlayerCharacter player, List<MapMarker> orbitalBodies,
                           List<CharacterMarker> characters, int selectedRadarRangeKm) {
        this.player = player;
        this.orbitalBodies = orbitalBodies != null ? orbitalBodies : new ArrayList<>();
        this.characters = characters != null ? characters : new ArrayList<>();
        this.selectedRadarRangeKm = selectedRadarRangeKm;
        updateObjectData();
    }

    public void render(float deltaTime) {
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);

        // Clear the screen
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Use shader program
        glUseProgram(shaderProgram);

        // Create transformations
        try (MemoryStack stack = stackPush()) {
            // Get framebuffer size correctly
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            int width = widthBuffer.get(0);
            int height = heightBuffer.get(0);

            // Projection matrix
            float aspectRatio = (float) width / (float) height;
            FloatBuffer projection = stack.mallocFloat(16);
            perspective(projection, (float) Math.toRadians(45.0f), aspectRatio, 0.1f, 1000.0f);

            // View matrix (camera)
            FloatBuffer view = stack.mallocFloat(16);
            cameraAngleX += deltaTime * 10.0f; // Slowly rotate the camera
            float camX = (float) (Math.sin(cameraAngleX) * cameraDistance);
            float camY = cameraHeight;
            float camZ = (float) (Math.cos(cameraAngleX) * cameraDistance);
            lookAt(view, camX, camY, camZ, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

            // Model matrix (identity)
            FloatBuffer model = stack.mallocFloat(16);
            identity(model);

            // Set uniforms
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "projection"), false, projection);
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "view"), false, view);
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, model);
        }

        // Draw grid
        glBindVertexArray(vaoGrid);
        glDrawArrays(GL_LINES, 0, 1000); // Adjust count as needed

        // Draw objects
        glBindVertexArray(vaoObjects);
        glDrawArrays(GL_POINTS, 0, objectCount);

        // Unbind
        glBindVertexArray(0);

        // Unuse shader program
        glUseProgram(0);
    }

    public void cleanup() {
        // Clean up VBOs and VAOs
        glDeleteBuffers(vboGrid);
        glDeleteBuffers(vboObjects);
        glDeleteVertexArrays(vaoGrid);
        glDeleteVertexArrays(vaoObjects);

        // Clean up shader program
        glDeleteProgram(shaderProgram);
    }

    // Helper method to create a perspective matrix
    private void perspective(FloatBuffer mat, float fov, float aspect, float near, float far) {
        float tanHalfFovy = (float) Math.tan(fov / 2.0f);

        mat.put(0, 1.0f / (aspect * tanHalfFovy));
        mat.put(1, 0.0f);
        mat.put(2, 0.0f);
        mat.put(3, 0.0f);

        mat.put(4, 0.0f);
        mat.put(5, 1.0f / tanHalfFovy);
        mat.put(6, 0.0f);
        mat.put(7, 0.0f);

        mat.put(8, 0.0f);
        mat.put(9, 0.0f);
        mat.put(10, -(far + near) / (far - near));
        mat.put(11, -1.0f);

        mat.put(12, 0.0f);
        mat.put(13, 0.0f);
        mat.put(14, -(2.0f * far * near) / (far - near));
        mat.put(15, 0.0f);
    }

    // Helper method to create a lookAt matrix
    private void lookAt(FloatBuffer mat, float eyeX, float eyeY, float eyeZ,
                        float centerX, float centerY, float centerZ,
                        float upX, float upY, float upZ) {
        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;

        // Normalize f
        float rlf = 1.0f / (float) Math.sqrt(fx*fx + fy*fy + fz*fz);
        fx *= rlf;
        fy *= rlf;
        fz *= rlf;

        // Normalize up
        float rlu = 1.0f / (float) Math.sqrt(upX*upX + upY*upY + upZ*upZ);
        upX *= rlu;
        upY *= rlu;
        upZ *= rlu;

        // Calculate s = f x up
        float sx = fy * upZ - fz * upY;
        float sy = fz * upX - fx * upZ;
        float sz = fx * upY - fy * upX;

        // Normalize s
        float rls = 1.0f / (float) Math.sqrt(sx*sx + sy*sy + sz*sz);
        sx *= rls;
        sy *= rls;
        sz *= rls;

        // Calculate u = s x f
        float ux = sy * fz - sz * fy;
        float uy = sz * fx - sx * fz;
        float uz = sx * fy - sy * fx;

        mat.put(0, sx);
        mat.put(1, ux);
        mat.put(2, -fx);
        mat.put(3, 0.0f);

        mat.put(4, sy);
        mat.put(5, uy);
        mat.put(6, -fy);
        mat.put(7, 0.0f);

        mat.put(8, sz);
        mat.put(9, uz);
        mat.put(10, -fz);
        mat.put(11, 0.0f);

        mat.put(12, 0.0f);
        mat.put(13, 0.0f);
        mat.put(14, 0.0f);
        mat.put(15, 1.0f);

        // Apply translation
        translate(mat, -eyeX, -eyeY, -eyeZ);
    }

    // Helper method to translate a matrix
    private void translate(FloatBuffer mat, float x, float y, float z) {
        mat.put(12, mat.get(0) * x + mat.get(4) * y + mat.get(8) * z + mat.get(12));
        mat.put(13, mat.get(1) * x + mat.get(5) * y + mat.get(9) * z + mat.get(13));
        mat.put(14, mat.get(2) * x + mat.get(6) * y + mat.get(10) * z + mat.get(14));
        mat.put(15, mat.get(3) * x + mat.get(7) * y + mat.get(11) * z + mat.get(15));
    }

    // Helper method to create an identity matrix
    private void identity(FloatBuffer mat) {
        mat.put(0, 1.0f);
        mat.put(1, 0.0f);
        mat.put(2, 0.0f);
        mat.put(3, 0.0f);

        mat.put(4, 0.0f);
        mat.put(5, 1.0f);
        mat.put(6, 0.0f);
        mat.put(7, 0.0f);

        mat.put(8, 0.0f);
        mat.put(9, 0.0f);
        mat.put(10, 1.0f);
        mat.put(11, 0.0f);

        mat.put(12, 0.0f);
        mat.put(13, 0.0f);
        mat.put(14, 0.0f);
        mat.put(15, 1.0f);
    }
}