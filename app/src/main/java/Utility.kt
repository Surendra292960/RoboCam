import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.opengl.GLES20
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import java.io.IOException
import java.nio.IntBuffer

object Utility {

    private fun saveBitmap(context: Context, bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // Scoped Storage
        }

        val uri: Uri? = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Log.d("Screenshot", "Screenshot saved to $uri")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } ?: run {
            Log.e("Screenshot", "Failed to create MediaStore entry")
        }
    }


    fun takeScreenshot(context: Context, mHeight: Int, mWidth: Int) {
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
        saveBitmap(context, bitmap)
    }

    fun getScreenSize(context: Context): Point {
        val size: Point?
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val metrics = windowManager.currentWindowMetrics
            // Gets all excluding insets
            val windowInsets = metrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom

            // Legacy size that Display#getSize reports
            val bounds = metrics.bounds
            size = Point(
                bounds.width() - insetsWidth,
                bounds.height() - insetsHeight
            )
        } else {
            size = Point()
            windowManager.defaultDisplay.getRealSize(size)
        }

        return size
    }
}