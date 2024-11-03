package com.example.robocam.video_stream
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import com.example.robocam.video_stream.RecordableSurfaceView.RendererCallbacks

class MyGLSurfaceView(context: Context) : RecordableSurfaceView(context), RendererCallbacks {
    // Set the Renderer for drawing on the GLSurfaceView
    private val mRenderer = MyGLRenderer(context)
    private var mPreviousX = 0f
    private var mPreviousY = 0f

    init {
        rendererCallbacks = this
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        return true
    }

    override fun onSurfaceCreated() {
        mRenderer.onSurfaceCreated(null, null)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        mRenderer.onSurfaceChanged(null, width, height)
    }

    override fun onSurfaceDestroyed() {
    }

    override fun onContextCreated() {
    }

    override fun onPreDrawFrame() {
    }

    override fun onDrawFrame() {
        mRenderer.onDrawFrame(null)
    }
}