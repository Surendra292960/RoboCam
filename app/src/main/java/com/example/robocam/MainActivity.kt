package com.example.robocam
import android.Manifest
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.robocam.opengl.MyCamera
import com.example.robocam.opengl.MyGLSurfaceView
import com.example.robocam.opengl.Permissions


class MainActivity : ComponentActivity() {
    private var glSurfaceView: MyGLSurfaceView? = null
    private var mCamera: MyCamera? = null
    private var mSurface: SurfaceTexture? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCamera = MyCamera()
        glSurfaceView = MyGLSurfaceView(this, mCamera!!)
        setContentView(glSurfaceView)

        setContent {
            Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                addAlbum()
                CustomView(mCamera!!)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun addAlbum(){
        val permissions = Permissions(
            this,
            arrayListOf(
                Manifest.permission.CAMERA),
            23)
        permissions.checkPermissions()
    }
}



@Composable
fun CustomView(mCamera: MyCamera) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Creates view
            MyGLSurfaceView(context, mCamera).apply {

            }
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary
        }
    )
}


/*
class MainActivity : Activity()*/
/*, SurfaceHolder.Callback, PreviewCallback *//*
{
    // private var mCamera: Camera? = null
    private var mView: SurfaceView? = null
    private var mHolder: SurfaceHolder? = null
    private lateinit var mData: ByteArray
    private lateinit var mDataRGB8888: IntArray

    private var mX: Int = 0
    private var mY: Int = 0
    private var pixelFormat = 0
    private var mGLSurfaceView: GLSurfaceView? = null
    private var mFrame: ViewGroup? = null
    private var mRenderer: TextureRenderer? = null

    private var glSurfaceView: MyGLSurfaceView? = null
    private var mCamera: MyCamera? = null

    @Composable
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        */
/*

                requestWindowFeature(Window.FEATURE_NO_TITLE)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                setContentView(R.layout.activity_main)

                mGLSurfaceView = GLSurfaceView(this)
                val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val configurationInfo = activityManager.deviceConfigurationInfo
                val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

                if (supportsEs2) {
                    mRenderer = TextureRenderer(this)
                    mGLSurfaceView!!.setEGLContextClientVersion(2)
                    mGLSurfaceView!!.setRenderer(mRenderer)
                    mGLSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                } else {
                    return
                }

                mFrame = findViewById<View>(R.id.main_layout) as ViewGroup
                mFrame!!.addView(mGLSurfaceView)

                mView = SurfaceView(this)
                mHolder = mView!!.holder
                mHolder!!.addCallback(this)

                mFrame!!.addView(mView)
        *//*


        Surface(modifier = Modifier) {
            mCamera = MyCamera()

            glSurfaceView = MyGLSurfaceView(this, mCamera!!)

            setContentView(glSurfaceView)
        }

    }

    */
/* override fun onResume() {
         super.onResume()
         // Open the default i.e. the first rear facing camera.
         mCamera = Camera.open()
         mCamera!!.startPreview()
     }

     override fun onPause() {
         super.onPause()
         mCamera!!.setPreviewCallback(null)
         mCamera!!.stopPreview()
         mCamera!!.release()
         mCamera = null
     }


     override fun surfaceCreated(holder: SurfaceHolder) {
         try {
             if (mCamera != null) mCamera!!.setPreviewDisplay(holder)
         } catch (exception: Exception) {
             Log.d("TAG", "surfaceCreated: Exception ")
         }
     }

     override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
         if (mCamera != null) {
             var parameters = mCamera!!.parameters
             pixelFormat = parameters.previewFormat

             mX = mFrame!!.width
             mY = mFrame!!.height
             parameters = mCamera!!.parameters
             val size = parameters.previewSize

             mX = size.width
             mY = size.height

             mData = ByteArray(mX * mY * 3 / 2)
             mCamera!!.addCallbackBuffer(mData)
             mCamera!!.setPreviewCallback(this)

             mDataRGB8888 = IntArray(mX * mY)
         }
     }


     override fun surfaceDestroyed(holder: SurfaceHolder) {

     }

     @Deprecated("Deprecated in Java")
     override fun onPreviewFrame(data: ByteArray, camera: Camera) {
         val parameters = mCamera!!.parameters
         val s = parameters.previewSize

         mRenderer!!.drawFrame(s.width, s.height, data)
         mGLSurfaceView!!.requestRender()
         mCamera!!.addCallbackBuffer(mData)

         Log.d("TAG", "onPreviewFrame: $mFrame")
     }*//*

}*/
