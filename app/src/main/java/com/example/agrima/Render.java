package com.example.agrima;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.graphics.Bitmap;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

public class Render implements GLSurfaceView.Renderer {

    private final Context context;
    private int programId;
    private int textureId = -1;
    private FloatBuffer vertexBuffer, texCoordBuffer;
    private Bitmap currentFrame;
    private int mode = 0;
    public void setMode(int mode) {
        this.mode = mode;
    }

    public Render(Context context) {
        this.context = context;
    }

    public void updateFrame(Bitmap frame) {
        this.currentFrame = frame;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);

        programId = compileAndLinkShaders();
        setupQuad();

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(programId);

        if (currentFrame != null) {
            Log.d("RENDER", "Uploading texture");
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, currentFrame, 0);
            currentFrame.recycle();
            currentFrame = null;
        }

        int texUniform = GLES20.glGetUniformLocation(programId, "u_Texture");
        GLES20.glUniform1i(texUniform, 0);


        int shaderModeLocation = GLES20.glGetUniformLocation(programId, "u_Mode");
        GLES20.glUniform1i(shaderModeLocation, mode);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        int posHandle = GLES20.glGetAttribLocation(programId, "a_Position");
        int texHandle = GLES20.glGetAttribLocation(programId, "a_TexCoord");

        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(texHandle);
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(posHandle);
        GLES20.glDisableVertexAttribArray(texHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    private void setupQuad() {
        float[] vertices = {
                -1f, -1f,
                1f, -1f,
                -1f,  1f,
                1f,  1f
        };
        float[] texCoords = {
                0f, 1f,
                1f, 1f,
                0f, 0f,
                1f, 0f
        };

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(texCoords).position(0);
    }

    private int compileAndLinkShaders() {
        String vShaderCode = Shade.loadShaderFromRaw(context, R.raw.vertex);
        String fShaderCode = Shade.loadShaderFromRaw(context, R.raw.fragment);

        int vShader = Shade.compileShader(GLES20.GL_VERTEX_SHADER, vShaderCode);
        int fShader = Shade.compileShader(GLES20.GL_FRAGMENT_SHADER, fShaderCode);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            String log = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Shader program linking failed: " + log);
        }

        return program;
    }
}

