package com.example.robocam.opengl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class TextureRenderer internal constructor(private val mActivityContext: Context) :
    GLSurfaceView.Renderer {
    /** This will be used to pass in model position information.  */
    private var mPositionHandle = 0
    private var mTexCoordinateHandle = 0

    private var mTextureUniformHandle0 = 0
    private var mTextureUniformHandle1 = 0

    private var mTexture0Id = 0
    private var mTexture1Id = 0

    private var mProgramHandle = 0


    private val mBytesPerFloat = 4

    /** Size of the position data in elements.  */
    private val mPositionDataSize = 3
    private val mTexCoordinateDataSize = 2

    /** Store our model data in a float buffer.  */
    private val mScreenPosition: FloatBuffer
    private val mTextureCoordinate: FloatBuffer

    private var mImage: Bitmap? = null

    @get:Synchronized
    private var buffer: ByteBuffer? = null
    private var mWidth = 0
    private var mHeight = 0

    init {
        val screenPosition =
            floatArrayOf(
                -1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                1.0f, 1.0f, 0.0f
            )


        val textureCoordinateData =
            floatArrayOf( // Front face
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
            )

        // Initialize the buffers.
        mScreenPosition = ByteBuffer.allocateDirect(screenPosition.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureCoordinate = ByteBuffer.allocateDirect(textureCoordinateData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()

        mScreenPosition.put(screenPosition).position(0)
        mTextureCoordinate.put(textureCoordinateData).position(0)
    }


    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {

        val vertexShader =
            """attribute vec4 a_Position;            
            attribute vec2 a_TexCoordinate;       
                                                  
            varying vec2 v_TexCoordinate;         
                                                  
            void main()                           
            {                                     
                v_TexCoordinate = a_TexCoordinate; 
               gl_Position = a_Position;          
                                                   
            }                                     
            """ // normalized screen coordinates.

        val fragmentShader =
            """precision mediump float;								  
                                                         
            uniform sampler2D u_Texture0;                            
            uniform sampler2D u_Texture1;                            
            varying vec2 v_TexCoordinate;                            
                                                                     
            const vec3 offset = vec3(0.0625, 0.5, 0.5);              
            const mat3 coeffs = mat3(              				  
                1.164,  1.164,  1.164,              				  
                1.596, -0.813,  0.0,              					  
                0.0  , -0.391,  2.018 );              				  
                                                                      
            vec3 texture2Dsmart(vec2 uv)              				  
              {													  
                    return coeffs*(vec3(texture2D(u_Texture0, uv).r, texture2D(u_Texture1, uv).ra) - offset);  
              }              										  
                                                                      
            void main()                                              
            {                                                         
              gl_FragColor = vec4(texture2Dsmart(v_TexCoordinate), 1.0);
            }                                                         
            """


        // Load in the vertex shader.
        var vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader)
            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle)
            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle)
                vertexShaderHandle = 0
            }
        }
        if (vertexShaderHandle == 0) {
            throw RuntimeException("Error creating vertex shader.")
        }


        // Load in the fragment shader shader.
        var fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader)
            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle)
            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle)
                fragmentShaderHandle = 0
            }
        }
        if (fragmentShaderHandle == 0) {
            throw RuntimeException("Error creating fragment shader.")
        }


        // Create a program object and store the handle to it.
        mProgramHandle = GLES20.glCreateProgram()
        if (mProgramHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(mProgramHandle, vertexShaderHandle)

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(mProgramHandle, fragmentShaderHandle)
            // Bind attributes
            GLES20.glBindAttribLocation(mProgramHandle, 0, "a_Position")
            // Link the two shaders together into a program.
            GLES20.glLinkProgram(mProgramHandle)

            // Get the link status.
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(mProgramHandle)
                mProgramHandle = 0
            }
        }
        if (mProgramHandle == 0) {
            throw RuntimeException("Error creating program.")
        }

        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position")
        mTexCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate")
        mTextureUniformHandle0 = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture0")
        mTextureUniformHandle1 = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture1")

        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            mTexture0Id = textureHandle[0]
        } else {
            throw RuntimeException("Error loading texture.")
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture0Id)

        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            mTexture1Id = textureHandle[0]
        } else {
            throw RuntimeException("Error loading texture.")
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture1Id)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }


    @Synchronized
    fun drawFrame(width: Int, height: Int, buf: ByteArray /*final int [] imageBytes*/) {
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(buf.size)
        }

        buffer!!.clear()
        buffer!!.put(buf)
        //mBuffer;
        buffer!!.position(0)

        mWidth = width
        mHeight = height
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(mProgramHandle)

        mScreenPosition.position(0)
        GLES20.glVertexAttribPointer(
            mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            0, mScreenPosition
        )
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Pass in the color information
        mTextureCoordinate.position(0)
        GLES20.glVertexAttribPointer(
            mTexCoordinateHandle, mTexCoordinateDataSize, GLES20.GL_FLOAT, false,
            0, mTextureCoordinate
        )
        GLES20.glEnableVertexAttribArray(mTexCoordinateHandle)

        loadAndDrawTextureFromBuffer()

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }


    private fun loadAndDrawTextureFromBuffer() {
        val frameData = buffer
        if (frameData != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

            frameData.position(0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture0Id)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                mWidth, mHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameData
            )

            GLES20.glUniform1i(mTextureUniformHandle0, 0)


            //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)

            frameData.position(mWidth * mHeight)
            val pos = frameData.position()
            val remain = frameData.remaining()

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture1Id)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_LUMINANCE_ALPHA,
                mWidth / 2,
                mHeight / 2,
                0,
                GLES20.GL_LUMINANCE_ALPHA,
                GLES20.GL_UNSIGNED_BYTE,
                frameData
            )
            frameData.position(0)

            GLES20.glUniform1i(mTextureUniformHandle1, 1)
        }
    }

}