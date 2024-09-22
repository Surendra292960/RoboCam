package com.example.robocam.opengl

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import java.io.IOException

class MyCamera {
    private var mCamera: Camera? = null
    private var mCameraParams: Camera.Parameters? = null
    private var running = false

    fun start(surface: SurfaceTexture?) {
        Log.v(LOG_TAG, "Starting Camera")

        mCamera = Camera.open(0)
        mCameraParams = mCamera!!.getParameters()
        Log.v(
            LOG_TAG,
            mCameraParams!!.previewSize.width.toString() + " x " + mCameraParams!!.previewSize.height.toString()
        )

        try {
            mCamera!!.setPreviewTexture(surface)
            mCamera!!.startPreview()
            running = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        if (running) {
            Log.v(LOG_TAG, "Stopping Camera")
            mCamera!!.stopPreview()
            mCamera!!.release()
            running = false
        }
    }

    companion object {
        private const val LOG_TAG = "MyCamera"
    }
}