package com.example.robocam.video_stream
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

class Triangle(val context: Context) {
    var result = false
        set(value) {
            field = value
            if (value) overlayStartTime = System.currentTimeMillis() // Set start time when result becomes true
        }

    private var program: Int = 0
    private var positionAttrib: Int = 0
    private var texCoordAttrib: Int = 0
    private var textureSamplerLocation: Int = 0
    private var vertexBuffer: Int = 0
    private var texCoordBuffer: Int = 0
    private var textureID: Int = 0

    private var dialogTextureID: Int = 0
    private var dialogVertexBuffer: Int = 0
    private var dialogTexCoordBuffer: Int = 0

    // Scale for icon size (adjust these values for desired icon dimensions)
    private var iconWidthScale = 0.5f  // 30% of screen width
    private var iconHeightScale = 0.5f // 30% of screen height
    private var dialogAspectRatio: Float = 1f
    //private var iconHeightScale = iconWidthScale / dialogAspectRatio

    // Time control
    private val overlayDuration = 5000L // 5 seconds in milliseconds
    private var overlayStartTime: Long = 0

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
        uniform vec4 vColor;
        uniform sampler2D textureSampler;
        void main() {
             gl_FragColor = texture2D(textureSampler, TexCoordOut);
        }
    """.trimIndent()

    private val verticesCoords = floatArrayOf(
        -1.0f, 1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        1.0f, 1.0f, 0.0f,
    )

    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f,
    )

    // Centered icon vertex coordinates
    private val dialogVerticesCoords = floatArrayOf(
        -iconWidthScale, iconHeightScale, - 0.2f,  // Top-left
        iconWidthScale, iconHeightScale, - 0.2f,   // Top-right
        iconWidthScale, -iconHeightScale, - 0.2f,  // Bottom-right
        -iconWidthScale, -iconHeightScale, - 0.2f  // Bottom-left
    )

    private val dialogTextureCoords = floatArrayOf(
        0.0f, 0.0f,  // Top-left
        1.0f, 0.0f,  // Top-right
        1.0f, 1.0f,  // Bottom-right
        0.0f, 1.0f   // Bottom-left
    )

    init {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        positionAttrib = GLES20.glGetAttribLocation(program, "position")
        texCoordAttrib = GLES20.glGetAttribLocation(program, "texCoord")
        textureSamplerLocation = GLES20.glGetUniformLocation(program, "textureSampler")

        // Setup vertex and texture coordinate buffers
        vertexBuffer = setupBuffer(verticesCoords)
        texCoordBuffer = setupBuffer(textureCoords)

        // Setup main texture
        textureID = setupTexture(context, "models/image.png")

        // Setup icon texture, vertices, and texture coordinates
        //iconTextureID = setupTexture(context, "models/mind.png")
        //iconTextureID = setupTexture(context, "models/mind.png")
        dialogVertexBuffer = setupBuffer(dialogVerticesCoords)
        dialogTexCoordBuffer = setupBuffer(dialogTextureCoords)
    }
    fun setupDialogTexture(/*bitmap: Bitmap?*/){
       // dialogTextureID = setupTextureDialog(bitmap)
        //dialogTextureID = TextureHelper.loadText(context, "01234");
        dialogTextureID = TextureHelper.loadText(context, "Robot Camera", ("""
     This is the description of the robot camera. It has multiple features, including AI integration, live streaming, and more.
     Explore the possibilities!
     """.trimIndent()))
    }


    private fun compileShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            checkGLError("Compile Shader")
        }
    }

    private fun setupBuffer(data: FloatArray): Int {
        val bufferArray = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, bufferArray)
        val bufferID = bufferArray[0]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferID)
        val vertexBuffer = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(data).position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        checkGLError("Setup Buffer")
        return bufferID
    }

    private fun setupTexture(context: Context, assetPath: String): Int {
        val textureHandle = IntBuffer.allocate(1)
        GLES20.glGenTextures(1, textureHandle)
        val textureID = textureHandle[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        val bitmap = BitmapFactory.decodeStream(context.assets.open(assetPath))
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        checkGLError("Setup Texture")
        return textureID
    }

    fun setDialogDimensions(width: Int, height: Int) {
        dialogAspectRatio = width.toFloat() / height.toFloat()
        iconWidthScale = 0.5f // Adjust scale as needed
    }

    private fun setupTextureDialog(bitmap: Bitmap): Int {
        val textureHandle = IntBuffer.allocate(1)
        GLES20.glGenTextures(1, textureHandle)
        val textureID = textureHandle[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        //  bitmap.recycle()
        checkGLError("Setup Texture")
        return textureID
    }

    fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)
        drawMain()
        Log.d("TAG", "draw Image Icon: $result")
        // Check if 5 seconds have passed since overlay display started
        if (result && System.currentTimeMillis() - overlayStartTime <= overlayDuration) {
            drawAlertDilog()
        } else {
            result = false  // Disable icon display after 5 seconds
        }

        // Clean up
        GLES20.glDisableVertexAttribArray(positionAttrib)
        GLES20.glDisableVertexAttribArray(texCoordAttrib)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)  // Re-enable depth testing if needed
        GLES20.glDisable(GLES20.GL_BLEND)  // Disable blending

        checkGLError("Draw")
    }

    private fun drawMain() {
        // Draw main texture
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        GLES20.glUniform1i(textureSamplerLocation, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

    private fun drawAlertDilog(){

        // Prepare for icon overlay
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)  // Disable depth testing to ensure overlay
        GLES20.glEnable(GLES20.GL_BLEND)  // Enable blending for transparency
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Draw icon overlay in center
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dialogVertexBuffer)
        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dialogTexCoordBuffer)
        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, dialogTextureID)
        GLES20.glUniform1i(textureSamplerLocation, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

    fun cleanup() {
        // Delete vertex buffer
        if (vertexBuffer != 0) {
            GLES20.glDeleteBuffers(1, intArrayOf(vertexBuffer), 0)
        }

        // Delete texture coordinate buffer
        if (texCoordBuffer != 0) {
            GLES20.glDeleteBuffers(1, intArrayOf(texCoordBuffer), 0)
        }

        // Delete dialog vertex buffer
        if (dialogVertexBuffer != 0) {
            GLES20.glDeleteBuffers(1, intArrayOf(dialogVertexBuffer), 0)
        }

        // Delete dialog texture coordinate buffer
        if (dialogTexCoordBuffer != 0) {
            GLES20.glDeleteBuffers(1, intArrayOf(dialogTexCoordBuffer), 0)
        }

        // Delete textures
        if (textureID != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(textureID), 0)
        }

        if (dialogTextureID != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(dialogTextureID), 0)
        }
    }

    private fun checkGLError(tag: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("Triangle", "$tag - OpenGL Error: $error")
        }
    }
}

