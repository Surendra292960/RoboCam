package com.example.robocam.opengl

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.example.robocam.utils.Utility.checkFramebufferStatus
import com.example.robocam.utils.Utility.checkGLError
import com.example.robocam.utils.Utility.compileShader
import com.example.robocam.utils.Utility.glLinkProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class DirectVideo(private val texture: Int) {
    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec2 inputTextureCoordinate;" +
            "varying vec2 textureCoordinate;" +
            "void main()" +
            "{" +
            "gl_Position = vPosition;" +
            "textureCoordinate = inputTextureCoordinate;" +
            "}"

    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;varying vec2 textureCoordinate;
        uniform samplerExternalOES s_texture;
        void main() {  gl_FragColor = texture2D( s_texture, textureCoordinate );
        }
        """.trimIndent()

    private val vertexBuffer: FloatBuffer
    private val textureVerticesBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private val mProgram: Int
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0


    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    init {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(squareCoords)
        vertexBuffer.position(0)

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        val bb2 = ByteBuffer.allocateDirect(textureVertices.size * 4)
        bb2.order(ByteOrder.nativeOrder())
        textureVerticesBuffer = bb2.asFloatBuffer()
        textureVerticesBuffer.put(textureVertices)
        textureVerticesBuffer.position(0)

        val vertexShader = MyGLSurfaceView.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader =
            MyGLSurfaceView.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram() // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(mProgram) // creates OpenGL ES program executables

    }

    fun draw() {
        GLES20.glUseProgram(mProgram)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture)

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the <insert shape here> coordinate data
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
        GLES20.glVertexAttribPointer(
            mTextureCoordHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            textureVerticesBuffer
        )

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
    }

    companion object {
        // number of coordinates per vertex in this array
        private const val COORDS_PER_VERTEX = 2

        var squareCoords: FloatArray = floatArrayOf(
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
        )

        var textureVertices: FloatArray = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
        )
    }
}