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
        mCameraParams = mCamera!!.parameters
        Log.d(
            "Load Camera Data",
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

/*    @SuppressLint("MissingPermission")
    private fun startCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        try {
            val cameraId = manager!!.cameraIdList[0] // Get the first camera
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    // Start capturing frames
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }*/
}

