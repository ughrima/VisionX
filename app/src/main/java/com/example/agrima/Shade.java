package com.example.agrima;

import android.content.Context;
import android.opengl.GLES20;

import java.io.InputStream;

public class Shade {
    public static String loadShaderFromRaw(Context context, int rawId) {
        try {
            InputStream is = context.getResources().openRawResource(rawId);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Shader load failed", e);
        }
    }

    public static int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            throw new RuntimeException("Shader compile error: " + GLES20.glGetShaderInfoLog(shader));
        }
        return shader;
    }
}
