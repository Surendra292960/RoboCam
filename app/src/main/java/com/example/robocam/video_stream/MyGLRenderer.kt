package com.example.robocam.video_stream
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.robocam.video_stream.Square
import com.example.robocam.video_stream.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(val context: Context) : GLSurfaceView.Renderer {
    private var mSquare: Square? = null
    private var mTriangle: Triangle? = null
    private val mMVPMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mSquare = Square(context)
        mTriangle = Triangle()
    }

    override fun onDrawFrame(unused: GL10?) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // Draw square
        mSquare?.draw(mMVPMatrix)
        mTriangle?.draw(mMVPMatrix)
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height)
    }
}