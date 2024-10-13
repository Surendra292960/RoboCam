package com.example.robocam.opengl.robo_cam
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.robocam.opengl.MyCamera
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class MyGLSurfaceView(context: Context?, var client: MyCamera, val flag: Boolean) : GLSurfaceView(context), GLSurfaceView.Renderer {

    private var program: Int = 0
    private var positionAttrib: Int = 0
    private var texCoordAttrib: Int = 0
    private var vertexBuffer: Int = 0
    private var texCoordBuffer: Int = 0
    private var textureID: Int = 0
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var mSurface: SurfaceTexture? = null
    private var vertexShader = 0
    private var fragmentShader = 0

    private val vertexShaderSource = """
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
    """

    // Initialize vertex buffer with data
    val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f,  // bottom left
        1.0f, -1.0f, 0.0f,  // bottom right
        1.0f, 1.0f, 0.0f,   // top right
        -1.0f, 1.0f, 0.0f   // top left
    )

    // Initialize texture coordinate buffer with data
    val texCoords = floatArrayOf(
        0.0f, 0.0f,  // bottom left
        1.0f, 0.0f,  // bottom right
        1.0f, 1.0f,  // top right
        0.0f, 1.0f   // top left
    )

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onDrawFrame(gl: GL10?) {
        try {
            /*client.camera()?.renderNextFrame()
            val data = client.camera()?.glTextureData() ?: return
            textureID = data.id.toInt()
            textureWidth = data.width
            textureHeight = data.height*/
            textureID = createTexture()
            mSurface = SurfaceTexture(textureID)
            mSurface?.updateTexImage()
            client.start(mSurface)
            render()
            // Check for OpenGL errors
            checkGLError()
        } catch (e: Exception) {
            Log.e("VideoFrame", "Error fetching texture data", e)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.v("LOG_TAG", "Surface Changed")
        GLES20.glViewport(0, 0, width, height)
        // Check for OpenGL errors
        checkGLError()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Load shaders and create program
        vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glLinkProgram(program)
        // Check for OpenGL errors
        checkGLError()

        // Get attribute locations
        positionAttrib = GLES20.glGetAttribLocation(program, "position")
        texCoordAttrib = GLES20.glGetAttribLocation(program, "texCoord")
        // Check for OpenGL errors
        checkGLError()

        // Create vertex buffer
        val vertexBufferArray = IntArray(1)
        GLES20.glGenBuffers(1, vertexBufferArray, 0)
        vertexBuffer = vertexBufferArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        // Check for OpenGL errors
        checkGLError()

        val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        // Check for OpenGL errors
        checkGLError()

        // Create texture coordinate buffer
        val texCoordBufferArray = IntArray(1)
        GLES20.glGenBuffers(1, texCoordBufferArray, 0)
        texCoordBuffer = texCoordBufferArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBuffer)
        // Check for OpenGL errors
        checkGLError()

        val texCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        texCoordBuffer.put(texCoords)
        texCoordBuffer.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texCoords.size * 4, texCoordBuffer, GLES20.GL_STATIC_DRAW)
        // Check for OpenGL errors
        checkGLError()
    }


    private fun render() {

        Log.d("TAG", "render glBindTexture : $textureID")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Use program
        GLES20.glUseProgram(program)
        // Check for OpenGL errors
        checkGLError()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, 0)
        // Check for OpenGL errors
        checkGLError()

        // Enable vertex attribute arrays and set pointers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, 0)
        // Check for OpenGL errors
        checkGLError()

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureWidth, textureHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        // Check for OpenGL errors
        checkGLError()

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
        // Check for OpenGL errors
        checkGLError()

        // Release the GL bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        // Check for OpenGL errors
        checkGLError()
    }

    private fun compileShader(type: Int, source: String): Int {
        // Load shader
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

    private fun checkGLError() {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("OpenGL Error", "Error: $error")
        }
    }

    private fun createTexture(): Int {
        val textures = IntArray(1)

        // generate one texture pointer and bind it as an external texture.
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])

        // No mip-mapping with camera source.
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())

        // Clamp to edge is only option.
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)

        return textures[0]
    }

    fun destroy() {
        GLES20.glDeleteProgram(program)
        GLES20.glDeleteShader(fragmentShader)
        GLES20.glDeleteShader(vertexShader)
    }
}