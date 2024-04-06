package jade;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class LevelEditorScene extends Scene {
    private String vertexShaderSrc = "#version 330 core\n" +
            "\n" +
            "layout (location=0) in vec3 aPos;\n" +
            "layout (location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main(){\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";

    private String fragmentShaderSrc = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main(){\n" +
            "    color = fColor;\n" +
            "}";

    private int vertexID, fragmentID, shaderProgram;

    private float[] vertexArray = {
            // position         // color
            0.5f, -0.5f,0.0f,   1.0f, 0.0f, 0.0f, 1.0f, // Bottom right
            -0.5f, 0.5f,0.0f,   0.0f, 1.0f, 0.0f, 1.0f, // Top left
            0.5f, 0.5f, 0.0f,   0.0f, 0.0f, 1.0f, 1.0f, // Top right
            -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // Bottom left
    };

    // Important: Must be in counterclockwise order
    private int[] elementArray = {
            2,1,0, //top right triangle
            0,1,3 //bottom left triangle
    };

    private int vaoID, vboID, eboID;

    public LevelEditorScene() {
    }

    @Override
    public void init(){
        vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        // Pass the shader source to the GPU
        GL20.glShaderSource(vertexID, vertexShaderSrc);
        GL20.glCompileShader(vertexID);

        // Check for errors in compilation
        int success = GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS);
        if (success == GL20.GL_FALSE) {
            int len = GL20.glGetShaderi(vertexID, GL20.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tVertex shader compilation failed.");
            System.out.println(GL20.glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // First load and compile the vertex shader
        fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        // Pass the shader source to the GPU
        GL20.glShaderSource(fragmentID, fragmentShaderSrc);
        GL20.glCompileShader(fragmentID);

        // Check for errors in compilation
        success = GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS);
        if (success == GL20.GL_FALSE) {
            int len = GL20.glGetShaderi(fragmentID, GL20.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tFragment shader compilation failed.");
            System.out.println(GL20.glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Link shaders and check for errors
        shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexID);
        GL20.glAttachShader(shaderProgram, fragmentID);
        GL20.glLinkProgram(shaderProgram);

        // Check for linking errors
        success = GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS);
        if (success == GL20.GL_FALSE) {
            int len = GL20.glGetProgrami(shaderProgram, GL20.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tLinking of shaders failed.");
            System.out.println(GL20.glGetProgramInfoLog(shaderProgram, len));
            assert false : "";
        }

        // ============================================================
        // Generate VAO, VBO, and EBO buffer objects, and send to GPU
        // ============================================================
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create VBO upload the vertex buffer
        vboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_STATIC_DRAW);

        // Create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, eboID);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL30.GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionsSize = 3;
        int colorSize = 4;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;
        GL30.glVertexAttribPointer(0, positionsSize, GL30.GL_FLOAT, false, vertexSizeBytes, 0);
        GL30.glEnableVertexAttribArray(0);

        GL30.glVertexAttribPointer(1, colorSize, GL30.GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);
        GL30.glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
        // Bind shader program
        GL30.glUseProgram(shaderProgram);
        // Bind the VAO that we're using
        GL30.glBindVertexArray(vaoID);

        // Enable the vertex attribute pointers
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        GL30.glDrawElements(GL30.GL_TRIANGLES, elementArray.length, GL30.GL_UNSIGNED_INT, 0);

        // Unbind everything
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);

        GL30.glBindVertexArray(0);

        GL30.glUseProgram(0);
    }

}
