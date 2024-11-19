package com.example.robocam.opengl.camer_rendering
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.robocam.utils.Utility.checkGLError
import com.example.robocam.utils.Utility.compileShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10

class RobotCameraRendering(val viewModel: ViewModel) {
    var result = false
        set(value) {
            field = value
            if (value) overlayDialogStartTime = System.currentTimeMillis() // Set start time when result becomes true
        }

    private var glTextCoords: FloatBuffer
    private var glVertices: FloatBuffer
    private var program: Int = 0
    private var positionAttrib: Int = 0
    private var texCoordAttrib: Int = 0
    private var vertexBuffer: Int = 0
    private var texCoordBuffer: Int = 0
    private var textureID: Int = 0
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var vertexShader = 0
    private var fragmentShader = 0

    private var dialogTextureID: Int = 0
    private var dialogVertexBuffer: Int = 0
    private var dialogTexCoordBuffer: Int = 0

    private var textureSamplerLocation: Int = 0

    // Scale for icon size (adjust these values for desired icon dimensions)
    private val dialogWidthScale = 0.5f  // 30% of screen width
    private val dialogHeightScale = 0.5f // 30% of screen height

    // Time control
    private val overlayDialogDuration = 5000L // 5 seconds in milliseconds
    private var overlayDialogStartTime: Long = 0

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

    // Vertex and texture coordinate data
    private var vertices: FloatArray = floatArrayOf(
        -1.0f, 1.0f, 0.0f,  // Top-left
        -1.0f, -1.0f, 0.0f,  // Bottom-left
        1.0f, -1.0f, 0.0f,  // Bottom-right
        1.0f, 1.0f, 0.0f   // Top-right
    )

    private val texCoords: FloatArray = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    // Centered icon vertex coordinates
    private val dialogVerticesCoords = floatArrayOf(
        -dialogWidthScale, dialogHeightScale, - 0.2f,  // Top-left
        dialogWidthScale, dialogHeightScale, - 0.2f,   // Top-right
        dialogWidthScale, -dialogHeightScale, - 0.2f,  // Bottom-right
        -dialogWidthScale, -dialogHeightScale, - 0.2f  // Bottom-left
    )

    private val dialogTextureCoords = floatArrayOf(
        0.0f, 0.0f,  // Top-left
        1.0f, 0.0f,  // Top-right
        1.0f, 1.0f,  // Bottom-right
        0.0f, 1.0f   // Bottom-left
    )


    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    init { // Load shaders and create program
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
        textureSamplerLocation = GLES20.glGetUniformLocation(program, "textureSampler")
        // Check for OpenGL errors
        checkGLError()

        // Create vertex buffer
        val vertexBufferArray = IntArray(1)
        GLES20.glGenBuffers(1, vertexBufferArray, 0)
        vertexBuffer = vertexBufferArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        // Check for OpenGL errors
        checkGLError()

        // Create vertex coordinate buffer
        glVertices = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        glVertices.put(vertices)
        glVertices.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, glVertices, GLES20.GL_STATIC_DRAW)
        // Check for OpenGL errors
        checkGLError()

        // Create texture coordinate buffer
        val texCoordBufferArray = IntArray(1)
        GLES20.glGenBuffers(1, texCoordBufferArray, 0)
        texCoordBuffer = texCoordBufferArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBuffer)
        // Check for OpenGL errors
        checkGLError()

        glTextCoords = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        glTextCoords.put(texCoords)
        glTextCoords.position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texCoords.size * 4, glTextCoords, GLES20.GL_STATIC_DRAW)
        // Check for OpenGL errors
        checkGLError()

        dialogVertexBuffer = setupBuffer(dialogVerticesCoords)
        dialogTexCoordBuffer = setupBuffer(dialogTextureCoords)

    /*    if (viewModel.counter.value==0){
            viewModel.counter.postValue(1)
            viewModel.startClient()
        }else{
            Log.d("DashBoardViewModel", "counter : ${viewModel.counter.value}  $textureID")
            viewModel.startAgain()
        }*/
    }

    fun setupDialogTexture(bitmap: Bitmap){
        dialogTextureID = setupTextureDialog(bitmap)
    }

    private fun setupBuffer(data: FloatArray): Int {
        val bufferArray = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, bufferArray)
        val bufferID = bufferArray[0]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferID)
        val vertexBuffer = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(data).position(0)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data.size * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        // Check for OpenGL errors
        checkGLError()

        return bufferID
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
        // Check for OpenGL errors
        checkGLError()
        return textureID
    }

    fun draw() {
        Log.d("TAG", "render glBindTexture : $textureID  $textureWidth  $textureHeight")
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)
        // Check for OpenGL errors
        checkGLError()
        renderCameraFrames()
        Log.d("TAG", "draw Image Icon: $result")
        // Check if 5 seconds have passed since overlay display started
        if (result && System.currentTimeMillis() - overlayDialogStartTime <= overlayDialogDuration) {
            drawDialog()
        } else {
            result = false  // Disable icon display after 5 seconds
        }

        // Clean up
        GLES20.glDisableVertexAttribArray(positionAttrib)
        GLES20.glDisableVertexAttribArray(texCoordAttrib)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)  // Re-enable depth testing if needed
        GLES20.glDisable(GLES20.GL_BLEND)  // Disable blending

        // Check for OpenGL errors
        checkGLError()
    }

    private fun renderCameraFrames(){
    /*    try {
            viewModel.robotClient?.camera()?.renderNextFrame()
            val data = viewModel.robotClient?.camera()?.glTextureData()?:return
            Log.d("VideoFrame draw ", "$data")
            textureID = data.id.toInt()
            textureWidth = data.width
            textureHeight = data.height

            // Check for OpenGL errors
            checkGLError()
        } catch (e: Exception) {
            Log.e("VideoFrame", "Error fetching texture data", e)
        }*/

        // Enable vertex coordinates attribute arrays and set pointers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, glVertices, GLES20.GL_STATIC_DRAW)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, 0)
        // Check for OpenGL errors
        checkGLError()

        // Enable texture coordinates attribute arrays and set pointers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texCoords.size * 4, glTextCoords, GLES20.GL_STATIC_DRAW)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, 0)
        // Check for OpenGL errors
        checkGLError()

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textureID)
        GLES20.glUniform1i(textureSamplerLocation, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
        // Check for OpenGL errors
        checkGLError()

        // Release the GL bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, 0)
        // Check for OpenGL errors
        checkGLError()
    }

    private fun drawDialog(){
        // Prepare for icon overlay
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)  // Disable depth testing to ensure overlay
        GLES20.glEnable(GLES20.GL_BLEND)  // Enable blending for transparency
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // Check for OpenGL errors
        checkGLError()

        // Draw icon overlay in center
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dialogVertexBuffer)
        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, 0)
        // Check for OpenGL errors
        checkGLError()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dialogTexCoordBuffer)
        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, 0)
        // Check for OpenGL errors
        checkGLError()

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, dialogTextureID)
        // GLES20.glUniform1i(textureSamplerLocation, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
        // Check for OpenGL errors
        checkGLError()
    }
}