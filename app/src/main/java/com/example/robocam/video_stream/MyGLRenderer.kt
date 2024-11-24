package com.example.robocam.video_stream
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.robocam.MainActivity
import com.example.robocam.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer(val context: Context) : GLSurfaceView.Renderer {
    private var mTriangle: Triangle? = null
    private var mSquare: ImageSquare? = null
    private var mWidth = 0
    private var mHeight = 0
    var isSave = false

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        // Set the background frame color

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        //mTriangle = Triangle(context)
        mSquare = ImageSquare(context = context)
    }

    override fun onDrawFrame(unused: GL10?) {
        // Draw triangle
       // mTriangle?.draw()
        // Draw square
         mSquare?.draw()


  /*      if (isSave){
            Log.d("TAG", "onDrawFrame: showDialog")
            // takeScreenshot()
            mTriangle?.result = false
            isSave = false
        }
        mTriangle?.setupDialogTexture(*//*bitmap = dialogBitmap!!*//*)*/

    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
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

        // Save the bitmap to a file
        saveBitmap(bitmap)
    }
}