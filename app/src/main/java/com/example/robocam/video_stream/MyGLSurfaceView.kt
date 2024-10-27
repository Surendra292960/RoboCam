package com.example.robocam.video_stream
import android.content.Context
import com.example.robocam.video_stream.RecordableSurfaceView.RendererCallbacks

class MyGLSurfaceView(context: Context) : RecordableSurfaceView(context), RendererCallbacks {
    // Set the Renderer for drawing on the GLSurfaceView
    private val mRenderer = MyGLRenderer(context)

    init {
        rendererCallbacks = this
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
