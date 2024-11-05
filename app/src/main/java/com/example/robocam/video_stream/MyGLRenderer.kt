package com.example.robocam.video_stream
import android.R.attr
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer(val context: Context) : GLSurfaceView.Renderer {
    private var mTriangle: Triangle? = null
    private var mSquare: Square? = null
    private var mWidth = 0
    private var mHeight = 0
    private var isSave = true

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    var angle: Float = 0f

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        // Set the background frame color

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        mTriangle = Triangle()
        mSquare = Square(context = context)
    }

    override fun onDrawFrame(unused: GL10?) {
        val scratch = FloatArray(16)

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        // Draw square
        mSquare!!.draw(mMVPMatrix)

        // Create a rotation for the triangle

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1.0f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)

        // Draw triangle
        //mTriangle!!.draw(scratch)

        if (isSave){
            takeScreenshot()
            isSave = false
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height)

        mWidth = width;
        mHeight = height;

       // val ratio = width.toFloat() / height

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }



    private fun saveBitmap(bitmap: Bitmap) {
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "screenshot.png")
            val fos = FileOutputStream(file)
            bitmap.compress(CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            Log.d("Screenshot", "Screenshot saved to " + file.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun takeScreenshot() {
        Log.d("TAG", "takeScreenshot: $mWidth  $mHeight")

        // Get the width and height of the view
        val width = mWidth
        val height = mHeight

        // Allocate a buffer to hold the pixel data
        val pixels = IntArray(width * height)
        val pixelBuffer = IntBuffer.wrap(pixels)
        pixelBuffer.position(0)

        // Read the pixels from the framebuffer
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer)

        // Create a bitmap from the pixel data
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(pixelBuffer)

        // Flip the bitmap vertically since OpenGL's origin is at the bottom-left
       // Matrix.flip(bitmap, true)
        //Matrix.setRotateM(mRotationMatrix, 0, 90f, 0f, 0f, 1.0f)

        // Save the bitmap to a file
        saveBitmap(bitmap)
    }
}