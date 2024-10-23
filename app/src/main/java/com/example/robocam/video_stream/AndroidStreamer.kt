/*
package com.example.robocam.video_stream

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class AndroidStreamer {
    private val FLOAT_SIZE_BYTES = 4
    private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
    private val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
    private val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

    private val _currActivity: Activity
    private var _streamConnection: VideoView? = null

    private var _cachedSurface: Surface? = null
    private var _cachedSurfaceTexture: SurfaceTexture? = null

    private var isNewFrame = false

    //open gl
    private val texWidth = 128
    private val texHeight = 128
    private val mMVPMatrix = FloatArray(16)
    private var mSTMatrix: FloatArray? = FloatArray(16)

    private var glProgram = 0
    private var muMVPMatrixHandle = 0
    private var muSTMatrixHandle = 0
    private var maPositionHandle = 0
    private var maTextureHandle = 0
    private var unityTextureID = -1
    private var mTextureId = -1 //surface texture id
    private var idFBO = -1
    private var idRBO = -1

    private val mTriangleVerticesData = floatArrayOf(
        // X, Y, Z, U, V
        -1.0f, -1.0f, 0f, 0f, 0f,
        1.0f, -1.0f, 0f, 1f, 0f,
        -1.0f, 1.0f, 0f, 0f, 1f,
        1.0f, 1.0f, 0f, 1f, 1f,
    )

    private val mTriangleVertices: FloatBuffer

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uSTMatrix;
        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;
        varying vec2 vTextureCoord;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTextureCoord = (uSTMatrix * aTextureCoord).xy;
        }
        """

    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTextureCoord);
        }
        """

    init {
        Log.d("Unity", "AndroidStreamer was initialized")

        _currActivity = UnityPlayer.currentActivity
        Vitamio.isInitialized(_currActivity)

        _currActivity.runOnUiThread {
            _streamConnection = VideoView(_currActivity)
            _currActivity.addContentView(_streamConnection, FrameLayout.LayoutParams(100, 100))
        }

        mTriangleVertices =
            ByteBuffer.allocateDirect(mTriangleVerticesData.size * FLOAT_SIZE_BYTES).order(
                ByteOrder.nativeOrder()
            ).asFloatBuffer()
        mTriangleVertices.put(mTriangleVerticesData).position(0)
        Matrix.setIdentityM(mSTMatrix, 0)

        initShaderProgram()
    }

    private fun initShaderProgram() {
        Log.d("Unity", "initShaderProgram")
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        glProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(glProgram, vertexShader)
        checkGlError("glAttachVertexShader")
        GLES20.glAttachShader(glProgram, fragmentShader)
        checkGlError("glAttachFragmentShader")
        GLES20.glLinkProgram(glProgram)
        checkGlError("glLinkProgram")

        maPositionHandle = GLES20.glGetAttribLocation(glProgram, "aPosition")
        checkLocation(maPositionHandle, "aPosition")
        maTextureHandle = GLES20.glGetAttribLocation(glProgram, "aTextureCoord")
        checkLocation(maTextureHandle, "aTextureCoord")

        muMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uMVPMatrix")
        checkLocation(muMVPMatrixHandle, "uVMPMatrix")
        muSTMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uSTMatrix")
        checkLocation(muSTMatrixHandle, "uSTMatrix")
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e("Unity", "Could not compile shader $shaderType:")
                Log.e("Unity", GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    private fun checkLocation(location: Int, label: String) {
        if (location < 0) {
            throw RuntimeException("Unable to locate '$label' in program")
        }
    }

    private fun checkGlError(op: String) {
        var error: Int
        while ((GLES20.glGetError().also { error = it }) != GLES20.GL_NO_ERROR) {
            Log.e("Unity", "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }

    private fun checkFrameBufferStatus() {
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        checkGlError("glCheckFramebufferStatus")
        when (status) {
            GLES20.GL_FRAMEBUFFER_COMPLETE -> Log.d("Unity", "complete")
            GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> Log.e("Unity", "incomplete attachment")
            GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> Log.e(
                "Unity",
                "incomplete missing attachment"
            )

            GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS -> Log.e("Unity", "incomplete dimensions")
            GLES20.GL_FRAMEBUFFER_UNSUPPORTED -> Log.e("Unity", "framebuffer unsupported")
            else -> Log.d("Unity", "default")
        }
    }

    private fun initGLTexture() {
        Log.d("Unity", "initGLTexture")
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        checkGlError("glGenTextures initGLTexture")
        mTextureId = textures[0]

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        checkGlError("glActiveTexture initGLTexture")
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        checkGlError("glBindTexture initGLTexture")

        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        checkGlError("glTexParameterf initGLTexture")
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        checkGlError("glTexParameterf initGLTexture")
    }

    fun GetTexturePtr(): Int {
        val bitmap = Bitmap.createBitmap(texWidth, texHeight, Bitmap.Config.ARGB_8888)
        for (x in 0 until texWidth) {
            for (y in 0 until texHeight) {
                bitmap.setPixel(x, y, Color.argb(155, 255, 50, 255))
            }
        }
        Log.d("Unity", "Bitmap is: $bitmap")

        val buffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buffer)

        //GLES20.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        //checkGlError("glEnable GetTexturePtr");
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        checkGlError("0")
        unityTextureID = textures[0]

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        checkGlError("1")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, unityTextureID)
        checkGlError("2")

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            texWidth,
            texHeight,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        checkGlError("12")
        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        //checkGlError("3");
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        checkGlError("4")
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        checkGlError("5")
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        checkGlError("6")
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        checkGlError("7")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        checkGlError("8")

        setupBuffers()
        Log.d("Unity", "texture id returned: $unityTextureID")

        return unityTextureID
    }

    private fun setupBuffers() {
        Log.d("Unity", "setupBuffers")

        //framebuffer
        val buffers = IntArray(1)
        GLES20.glGenFramebuffers(1, buffers, 0)
        checkGlError("9")
        idFBO = buffers[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, idFBO)
        checkGlError("10")

        //render buffer
        val rbuffers = IntArray(1)
        GLES20.glGenRenderbuffers(1, rbuffers, 0)
        checkGlError("glGenRenderBuffers setupBuffers")
        idRBO = rbuffers[0]
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, idRBO)
        checkGlError("glBindRenderBuffer setupBuffers")
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_RGBA4, texWidth, texHeight)
        checkGlError("glRenderBufferStorage setupBuffers")

        GLES20.glFramebufferRenderbuffer(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_RENDERBUFFER,
            idRBO
        )
        checkGlError("glFramebufferRenderbuffer setupBuffers")

        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            unityTextureID,
            0
        )
        checkGlError("glFrameBufferTexture2D")

        checkFrameBufferStatus()

        GLES20.glClearColor(1.0f, 0.5f, 0.0f, 1.0f)
        checkGlError("glClearColor setupBuffers")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        checkGlError("glClear setupBuffers")
    }

    fun DrawFrame() {
        if (isNewFrame && mSTMatrix != null) {
            val testBuffer = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, testBuffer, 0)

            Log.d("Unity", "DrawFrame binded = " + testBuffer[0] + " idFBO = " + idFBO)

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, idFBO)
            checkGlError("glBindFrameBuffer DrawFrame")

            GLES20.glClearColor(0.0f, 1.0f, 0.2f, 1.0f)
            checkGlError("glClearColor DrawFrame")
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            checkGlError("glClear DrawFrame")

            GLES20.glUseProgram(glProgram)
            checkGlError("glUseProgram DrawFrame")

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            checkGlError("glActiveTexture DrawFrame")
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
            checkGlError("glBindTexture DrawFrame")

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
            GLES20.glVertexAttribPointer(
                maTextureHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices
            )
            checkGlError("glVertexAttribPointer DrawFrame")
            GLES20.glEnableVertexAttribArray(maTextureHandle)
            checkGlError("glEnableVertexAttribArray DrawFrame")

            Matrix.setIdentityM(mMVPMatrix, 0)
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
            checkGlError("glUniformMatrix4fv MVP onFrameAvailable")
            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0)
            checkGlError("glUniformMatrix4fv ST onFrameAvailable")

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            checkGlError("glDrawArrays onFrameAvailable")

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            checkGlError("glBindFrameBuffer 0 onFrameAvailable")
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
            checkGlError("glBindTexture onFrameAvailable")

            isNewFrame = false
        }
    }

    fun LaunchStream(streamLink: String) {
        val path =
            streamLink //"http://dlqncdn.miaopai.com/stream/MVaux41A4lkuWloBbGUGaQ__.mp4"; //"rtmp://live.hkstv.hk.lxdns.com/live/hks";
        Log.i("Unity", "hop hop1 = $path")

        _currActivity.runOnUiThread {
            _streamConnection!!.setVideoPath(path)
            _streamConnection!!.setMediaController(MediaController(_currActivity))
            _streamConnection!!.requestFocus()

            _streamConnection!!.setOnErrorListener { mp, what, extra ->
                Log.i("Unity", "some error, I don't know. what = $what extra = $extra")
                false
            }

            _streamConnection!!.setOnPreparedListener {
                // optional need Vitamio 4.0
                Log.i("Unity", "hop hop5")
                //mediaPlayer.setPlaybackSpeed(1.0f);
            }

            initGLTexture()

            _cachedSurfaceTexture = SurfaceTexture(mTextureId)
            _cachedSurfaceTexture!!.setDefaultBufferSize(texWidth, texHeight)

            _cachedSurfaceTexture!!.setOnFrameAvailableListener(object : OnFrameAvailableListener {
                override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
                    synchronized(this) {
                        surfaceTexture.updateTexImage()
                        mSTMatrix = FloatArray(16)
                        surfaceTexture.getTransformMatrix(mSTMatrix)
                        isNewFrame = true
                    }
                }
            })

            _cachedSurface = Surface(_cachedSurfaceTexture)
            _streamConnection.setSurfaceToPlayer(_cachedSurface)
            Log.i("Unity", "You're the best around!")
        }
    }
}*/
