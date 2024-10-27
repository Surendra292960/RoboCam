/*
package com.example.videorecorder.recorder

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowMetrics
import androidx.core.content.FileProvider
import com.example.videorecorder.R
import java.io.File
import java.io.IOException
import java.util.Date

class OpenGLES20Activity : Activity() {
    private var mGLView: RecordableSurfaceView? = null

    private var mIsRecording = false

    private var mOutputFile: File? = null


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = MyGLSurfaceView(this)
        setContentView(mGLView)
    }

    override fun onPause() {
        super.onPause()
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView!!.pause()
    }

    override fun onResume() {
        super.onResume()

        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        if (PermissionsHelper.hasPermissions(this)) {
            // Note that order matters - see the note in onPause(), the reverse applies here.
            mGLView!!.resume()
            try {
                mOutputFile = createVideoOutputFile()
                val size = screenSize
                mOutputFile?.let { mGLView!!.initRecorder(it, size.x, size.y, null, null) }
            } catch (ioex: IOException) {
                Log.e(ContentValues.TAG, "Couldn't re-init recording", ioex)
            }
        } else {
            PermissionsHelper.requestPermissions(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private val screenSize: Point
        get() {
            var size: Point? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val metrics: WindowMetrics = windowManager.currentWindowMetrics
                // Gets all excluding insets
                val windowInsets: WindowInsets = metrics.getWindowInsets()
                val insets: Insets =
                    windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

                val insetsWidth = insets.right + insets.left
                val insetsHeight = insets.top + insets.bottom

                // Legacy size that Display#getSize reports
                val bounds: Rect = metrics.getBounds()
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

    private fun createVideoOutputFile(): File? {
        var tempFile: File? = null
        val filename = Date().time.toString() + ""
        var filesDir = ""

        try {
            filesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getExternalFilesDir(null)!!.path
            } else {
                getFilesDir().canonicalPath
            }

            val dirCheck = File("$filesDir/captures")

            if (!dirCheck.exists()) {
                dirCheck.mkdirs()
            }

            tempFile = File("$filesDir/captures/$filename.mp4")
        } catch (ioex: IOException) {
            Log.e(ContentValues.TAG, "Couldn't create output file", ioex)
        }

        return tempFile
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Note that order matters - see the note in onPause(), the reverse applies here.
        mGLView!!.resume()
        try {
            mOutputFile = createVideoOutputFile()
            val size = screenSize

            mOutputFile?.let { mGLView!!.initRecorder(it, size.x, size.y, null, null) }
        } catch (ioex: IOException) {
            Log.e(ContentValues.TAG, "Couldn't re-init recording", ioex)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mIsRecording) {
            mGLView!!.stopRecording()

            val contentUri = FileProvider.getUriForFile(
                this,
                "com.example.videorecorder.fileprovider",
                mOutputFile!!
            )

            //share(contentUri)
            mIsRecording = false
            mOutputFile = createVideoOutputFile()

            try {
                val screenWidth: Int = mGLView!!.width
                val screenHeight: Int = mGLView!!.height
                mOutputFile?.let {
                    mGLView!!.initRecorder(
                        it, screenWidth, screenHeight, null,
                        null
                    )
                }
            } catch (ioex: IOException) {
                Log.e(ContentValues.TAG, "Couldn't re-init recording", ioex)
            }
            item.setTitle("Record")
        } else {
            mGLView!!.startRecording()
            Log.v(ContentValues.TAG, "Recording Started")

            item.setTitle("Stop")
            mIsRecording = true
        }
        return true
    }


    private fun share(contentUri: Uri) {
        val shareIntent = Intent()
        shareIntent.setAction(Intent.ACTION_SEND)
        shareIntent.setType("video/mp4")
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(shareIntent, "Share with"))
    }
}*/
