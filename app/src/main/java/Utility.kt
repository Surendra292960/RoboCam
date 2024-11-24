import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager

object Utility {


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