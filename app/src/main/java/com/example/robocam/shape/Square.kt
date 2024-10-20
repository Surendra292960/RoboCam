package com.example.robocam.shape

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.view.TextureView
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by rich on 03/05/2015.
 */
class Square {
    private val vertexBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private val mCubeTextureCoordinates: FloatBuffer

    var color: FloatArray = floatArrayOf(1f, 1f, 1f, 1.0f)

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec2 a_TexCoordinate;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            " gl_Position = vPosition;" +
            " v_TexCoordinate = a_TexCoordinate;" +
            "}"

    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;uniform vec4 vColor;
        uniform samplerExternalOES u_Texture;
        varying vec2 v_TexCoordinate;
        void main() {
            gl_FragColor = (texture2D(u_Texture, v_TexCoordinate));
        }
        """.trimIndent()

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    private val mProgram: Int

    private var mPositionHandle = 0
    private var mColorHandle = 0
    private var mTextureUniformHandle = 0
    private var mTextureCoordinateHandle = 0
    private val mTextureCoordinateDataSize = 2

    private val vertexCount = squareCoords.size / COORDS_PER_VERTEX
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private val mTextureDataHandle: Int

    var textureCoordinates: FloatArray = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )

    var _camera: Camera
    var _textureView: TextureView? = null
    var textures: IntArray
    var _surface: SurfaceTexture

    init {
        val bb = ByteBuffer.allocateDirect( // (# of coordinate values * 4 bytes per float)
            squareCoords.size * 4
        )
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(squareCoords)
        vertexBuffer.position(0)

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect( // (# of coordinate values * 2 bytes per short)
            drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(
            textureCoordinates.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeTextureCoordinates.put(textureCoordinates).position(0)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram()

        textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)

        _surface = SurfaceTexture(textures[0])
        _camera = Camera.open()
        val previewSize = _camera.parameters.previewSize

        try {
            _camera.setPreviewTexture(_surface)
        } catch (ex: IOException) {
            // Console.writeLine (ex.Message);
        }

        val vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode)
        GLES20.glCompileShader(vertexShaderHandle)
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            //do check here
        }

        val fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode)
        GLES20.glCompileShader(fragmentShaderHandle)
        GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            //do check here
        }

        GLES20.glAttachShader(mProgram, vertexShaderHandle)
        GLES20.glAttachShader(mProgram, fragmentShaderHandle)
        GLES20.glBindAttribLocation(mProgram, 0, "a_Position")
        GLES20.glBindAttribLocation(mProgram, 0, "a_TexCoordinate")

        GLES20.glLinkProgram(mProgram)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            //do check here
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        mTextureDataHandle = textures[0]

        // Set filtering
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
    }

    fun draw() {
        _surface.updateTexImage()
        GLES20.glUseProgram(mProgram)

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture")
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position")
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color")
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureDataHandle)

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates)

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glUniform1i(mTextureUniformHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX: Int = 3
        var squareCoords: FloatArray = floatArrayOf(
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            -0.5f, 0.5f, 0.0f,  // top left
            0.5f, 0.5f, 0.0f
        ) // top right
    }
}