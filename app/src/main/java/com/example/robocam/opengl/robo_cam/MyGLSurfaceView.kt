package com.example.robocam.opengl.robo_cam

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.example.robocam.opengl.MyCamera
import com.example.robocam.utils.Utility.checkFramebufferStatus
import com.example.robocam.utils.Utility.compileShader
import com.example.robocam.utils.Utility.glLinkProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


internal class MyGLSurfaceView(context: Context?, var client: MyCamera, val flag: Boolean) :
    GLSurfaceView(context), GLSurfaceView.Renderer {

    private var program: Int = 0
    private var positionAttrib: Int = 0
    private var texCoordAttrib: Int = 0
    private var textureSamplerLocation: Int = 0
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
    """.trimIndent()

    private val fragmentShaderSource = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 TexCoordOut;
        uniform vec4 vColor;
        uniform sampler2D textureSampler;
       
        void main() {
             gl_FragColor = texture2D(textureSampler, TexCoordOut);
        }
        """.trimIndent()


    // Initialize vertex buffer with data
    private val verticesCoords = floatArrayOf(
        -1.0f, 1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        1.0f, 1.0f, 0.0f,
    )

    // Initialize texture coordinate buffer with data
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f,
    )

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onDrawFrame(gl: GL10?) {
        try {
       /* client.camera()?.renderNextFrame()
          val data = client.camera()?.glTextureData() ?: return
          textureID = data.id.toInt()
          textureWidth = data.width
          textureHeight = data.height*/
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
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Load shaders and create program
        vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        glLinkProgram(program)
        // Check for OpenGL errors
        checkGLError()

        // Get attribute locations
        positionAttrib = GLES20.glGetAttribLocation(program, "position")
        texCoordAttrib = GLES20.glGetAttribLocation(program, "texCoord")
        textureSamplerLocation = GLES20.glGetUniformLocation(program, "textureSampler")

        // Check for OpenGL errors
        checkGLError()

        // Create vertex buffer
        val vertexBufferArray = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, vertexBufferArray)
        vertexBuffer = vertexBufferArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        checkFramebufferStatus()
        // Check for OpenGL errors
        checkGLError()

        val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(verticesCoords.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexBuffer.put(verticesCoords)
        vertexBuffer.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            verticesCoords.size * 4,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        checkFramebufferStatus()
        // Check for OpenGL errors
        checkGLError()

        // Create texture coordinate buffer
        val texCoordBufferArray = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, texCoordBufferArray)
        texCoordBuffer = texCoordBufferArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBuffer)
        checkFramebufferStatus()
        // Check for OpenGL errors
        checkGLError()

        val texCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        texCoordBuffer.put(textureCoords)
        texCoordBuffer.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            textureCoords.size * 4,
            texCoordBuffer,
            GLES20.GL_STATIC_DRAW
        )
        checkFramebufferStatus()
        // Check for OpenGL errors
        checkGLError()

        // Read the texture.
        val textureBitmap = BitmapFactory.decodeStream(context.assets.open("models/mind.png"))

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0)

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

        textureBitmap.recycle()

    }


    private fun render() {
        Log.d("TAG", "render glBindTexture : $textureID  $textureWidth  $textureHeight")
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // Use program
        GLES20.glUseProgram(program)
        // Check for OpenGL errors
        checkGLError()

        // Enable vertex attribute arrays and set pointers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBuffer)
        checkFramebufferStatus()
        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, 0)
        // Check for OpenGL errors
        checkGLError()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        checkFramebufferStatus()
        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, 0)

        // Check for OpenGL errors
        checkGLError()

        // GL bind texture
        GLES20.glUniform1i(textureSamplerLocation, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID)
        GLES20.glUniform1i(textureSamplerLocation, 0)
        // Check for OpenGL errors
        checkGLError()

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

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

    private fun checkGLError() {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("OpenGL Error", "Error: $error")
        }
    }

    fun destroy() {
        GLES20.glDeleteProgram(program)
        GLES20.glDeleteShader(fragmentShader)
        GLES20.glDeleteShader(vertexShader)
    }
}