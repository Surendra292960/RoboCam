/*
package com.example.robocam.opengl.robo_cam

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.robocam.opengl.DirectVideo
import com.example.robocam.opengl.MyCamera
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


internal class MyGLSurfaceView(context: Context?, var client: MyCamera, val flag:Boolean) : GLSurfaceView(context), GLSurfaceView.Renderer {
    private var program: Int = 0
    private var positionAttrib: Int = 0
    private var texCoordAttrib: Int = 0
    private var textureID: Int = 0
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var mSurface: SurfaceTexture? = null

    val vertexBuffer: FloatBuffer
    val textureVerticesBuffer: FloatBuffer
    val drawListBuffer: ShortBuffer
    private var mDirectVideo: DirectVideo? = null

    */
/*private val vertexShaderSource = """
        attribute vec4 position;
        attribute vec2 texCoord;
        varying vec2 TexCoordOut;
        void main() {
            gl_Position = position;
            TexCoordOut = texCoord;
        }
        """

    private val fragmentShaderSource = """
        precision mediump float;
        varying vec2 TexCoordOut;
        uniform sampler2D textureSampler;
        void main() {
            gl_FragColor = texture2D(textureSampler, TexCoordOut);
        }
        """*//*


    private val vertexShaderSource = """
        attribute vec4 position;
        attribute vec2 texCoord;
        varying vec2 TexCoordOut;
        void main() {
            gl_Position = position;
            TexCoordOut = texCoord;
        }
        """.trimIndent()

    private val fragmentShaderSource = """
        precision mediump float;
        varying vec2 TexCoordOut;
        uniform sampler2D textureSampler;
        void main() {
            gl_FragColor = texture2D(textureSampler, TexCoordOut);
        }
        """.trimIndent()

    private val squareCoords = floatArrayOf(
        -1.0f, -1.0f, 0.0f,  // bottom left
        1.0f, -1.0f, 0.0f,  // bottom right
        1.0f, 1.0f, 0.0f,   // top right
        -1.0f, 1.0f, 0.0f   // top left
    )

    private val textureVertices = floatArrayOf(
        0.0f, 1.0f,  // bottom left
        1.0f, 1.0f,  // bottom right
        1.0f, 0.0f,  // top right
        0.0f, 0.0f   // top left
    )

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY


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

        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader =
            compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)

        program = GLES20.glCreateProgram() // create empty OpenGL ES Program
        GLES20.glAttachShader(program, vertexShader) // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(program) // creates OpenGL ES program executables
    }

    override fun onDrawFrame(gl: GL10?) {
        try {
        */
/*    client.camera()?.renderNextFrame()
            val data = client.camera()?.glTextureData() ?: return
            textureID = data.id.toInt()
            textureWidth = data.width
            textureHeight = data.height
            mSurface = SurfaceTexture(textureID)*//*

        } catch (e: Exception) {
            Log.e("VideoFrame", "Error fetching texture data", e)
        }

        val mtx = FloatArray(16)
        mSurface!!.updateTexImage()
        mSurface!!.getTransformMatrix(mtx)
        render()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.v("LOG_TAG", "Surface Changed")
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        val texture = createTexture()
        mDirectVideo = DirectVideo(texture)
        mSurface = SurfaceTexture(texture)
        client.start(mSurface)
    }

    private fun createTexture(): Int {
        val textures = IntArray(1)

        // generate one texture pointer and bind it as an external texture.
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])

        // No mip-mapping with camera source.
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )

        // Clamp to edge is only option.
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE
        )

        return textures[0]
    }

    private fun render() {
        Log.d("TAG", "render glBindTexture : $textureID")
        GLES20.glUseProgram(program)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer[0].toInt())
        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GL10.GL_FLOAT, false, 0, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureVerticesBuffer[0].toInt())
        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GL10.GL_FLOAT, false, 0, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        GLES20.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        println("Shader : $shader status : ${compileStatus[0]}")
        if (compileStatus[0] == 0) {
            val logLength = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_INFO_LOG_LENGTH, logLength, 0)
            val log = ByteArray(logLength[0])
            GLES20.glGetShaderInfoLog(shader)
            println("Shader compilation error: ${String(log)}")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }
}*/
