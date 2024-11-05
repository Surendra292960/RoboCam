package com.example.robocam.video_stream
import android.app.AlertDialog
import android.content.Context
import android.graphics.PixelFormat
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.example.robocam.video_stream.RecordableSurfaceView.RendererCallbacks


class MyGLSurfaceView(context: Context) : RecordableSurfaceView(context), RendererCallbacks {
    // Set the Renderer for drawing on the GLSurfaceView
    private val mRenderer = MyGLRenderer(context)
    private var mPreviousX = 0f
    private var mPreviousY = 0f

    init {
        rendererCallbacks = this
        this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.holder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(false); //CODE TO SET VIDEO VIEW TO BACK
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        return super.onTouchEvent(event);
    }

    private fun showDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Dialog Title")
            .setMessage("This is a dialog message.")
            .setPositiveButton(android.R.string.ok, null)
            .show()
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