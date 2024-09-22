package com.example.robocam.opengl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class MyGLSurfaceView(context: Context?, private val mCamera: MyCamera) :
    GLSurfaceView(context),
    GLSurfaceView.Renderer {
    private var mSurface: SurfaceTexture? = null
    private var mDirectVideo: DirectVideo? = null

    init {
        setEGLContextClientVersion(2)

        setRenderer(this)
    }

    override fun onDrawFrame(gl: GL10?) {
        val mtx = FloatArray(16)
        mSurface!!.updateTexImage()
        mSurface!!.getTransformMatrix(mtx)

        mDirectVideo!!.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.v(LOG_TAG, "Surface Changed")
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.v(LOG_TAG, "Surface Created")
        val texture = createTexture()
        mDirectVideo = DirectVideo(texture)
        mSurface = SurfaceTexture(texture)
        mCamera.start(mSurface)
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

    companion object {
        private const val LOG_TAG = "MyGLSurfaceView"
        fun loadShader(type: Int, shaderCode: String?): Int {
            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)

            val shader = GLES20.glCreateShader(type)

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }
    }
}