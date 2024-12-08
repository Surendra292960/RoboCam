package com.example.robocam.video_stream
import android.content.Context
import com.example.robocam.MainViewModel
import com.example.robocam.video_stream.RecordableSurfaceView.RendererCallbacks


class MyGLSurfaceView(context: Context, viewModel: MainViewModel) : RecordableSurfaceView(context), RendererCallbacks{
    // Set the Renderer for drawing on the GLSurfaceView
    val mRenderer = MyGLRenderer(context, viewModel)

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