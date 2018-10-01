package net.excession.mygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.glGetError;

/**
 * Created by Jeremiah Biard on 9/30/18.
 */
public class Starfield {

    static float squareCoords[] = {
            -1f, 1f, 0.0f,      // Top left
            -1f, -1f, 0.0f,     // bottom left
            1f, -1f, 0.0f,      // bottom right
            1f, 1f, 0.0f };     // top right

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 TexCoordIn;" +
            "varying vec2 TexCoordOut;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  TexCoordOut = TexCoordIn;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "uniform vec4 vColor;\n" +
            "uniform sampler2D TexCoordIn;\n" +
            "uniform float scroll;\n" +
            "varying vec2 TexCoordOut;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(TexCoordIn, " +
            "      vec2(TexCoordOut.x, TexCoordOut.y + scroll));\n" +
            "}\n";

    private float texture[] = {
            -1f, 1f,
            -1f, -1f,
            1f, -1f,
            1f, 1f,
    };

    private int[] textures = new int[1];
    private final FloatBuffer textureBuffer;
    static final int COORDS_PER_TEXTURE = 2;
    public static int textureStride = COORDS_PER_TEXTURE * 4;
    // Add a new texture buffer to the Starfield constructor

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4;

    public Starfield() {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(texture.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textureBuffer = bb.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        // TODO: Figure out why the following call is failing :/
        int fragmentShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        IntBuffer linkSuccess = IntBuffer.allocate(1);
        GLES20.glLinkProgram(mProgram);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkSuccess);
        Log.e("Link Status", "Linking status = " + linkSuccess.get(0));
        if (linkSuccess.get(0) == GLES20.GL_FALSE) {
            throw new RuntimeException("Linking failed: " + GLES20.glGetProgramInfoLog(mProgram) + " | " + mProgram);
        }
    }

    public void draw(float[] mvpMatrix, float scroll) {

        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        int vsTextureCoord = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer);

        GLES20.glVertexAttribPointer(vsTextureCoord, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, textureStride, textureBuffer);

        GLES20.glEnableVertexAttribArray(vsTextureCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        int fsTexture = GLES20.glGetUniformLocation(mProgram, "TexCoordOut");
        int fsScroll = GLES20.glGetUniformLocation(mProgram, "scroll");
        GLES20.glUniform1i(fsTexture, 0);
        GLES20.glUniform1f(fsScroll, scroll);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GameRenderer.checkGlError("glUniformMatrix4fv");
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void loadTexture(int texture, Context context) {
        InputStream imagestream = context.getResources().openRawResource(texture);
        Bitmap bitmap = null;

        android.graphics.Matrix flip = new android.graphics.Matrix();
        flip.postScale(-1f, -1f);

        try {
            bitmap = BitmapFactory.decodeStream(imagestream);
        } catch (Exception e) {

        } finally {
            try {
                imagestream.close();
                imagestream = null;
            } catch (IOException e){

            }
        }

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }
}
