package com.example.robocam.video_stream
import Utility.takeScreenshot
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.robocam.MainViewModel
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer(val context: Context, val viewModel: MainViewModel) : GLSurfaceView.Renderer {
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

        if (viewModel.isDialogShowing.value==true){
            mSquare?.setupDialogTexture()
            viewModel.isDialogShowing.postValue(false)
        }

        if (viewModel.takeScreenShot.value==true){
            takeScreenshot(context, mHeight, mWidth)
            viewModel.takeScreenShot.postValue(false)
        }

    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        mWidth = width
        mHeight = height
    }
}