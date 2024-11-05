package com.example.robocam
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.robocam.joystick.JoyStickController
import com.example.robocam.opengl.Permissions
import com.example.robocam.utils.ViewRecorder
import com.example.robocam.video_stream.MyGLSurfaceView
import com.example.robocam.video_stream.PermissionsHelper
import com.example.robocam.video_stream.RecordableSurfaceView
import java.io.File
import java.io.IOException
import java.util.Date


var mView: View? = null
private var mGLView: RecordableSurfaceView? = null
private var mOutputFile: File? = null

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var mIsRecording = false


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LayoutInflater.from(this).inflate(R.layout.image, null, false)
        mView = layout.findViewById<LinearLayout>(R.id.root)
        setContent {
            Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
                OpenGLScreen().also {
                       Box(modifier = Modifier.fillMaxSize(),
                           contentAlignment = Alignment.Center){
                           JetStickUI(modifier = Modifier.align(Alignment.CenterStart), viewModel)
                       }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mGLView?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (PermissionsHelper.hasPermissions(this)) {
            // Note that order matters - see the note in onPause(), the reverse applies here.
            mGLView?.resume()
            try {
                mOutputFile = createVideoOutputFile(this)
                val size = getScreenSize(this)
                mGLView?.initRecorder(mOutputFile!!, size.x, size.y, null, null)
            } catch (ioex: IOException) {
                Log.e(ContentValues.TAG, "Couldn't re-init recording", ioex)
            }
        } else {
            PermissionsHelper.requestPermissions(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mGLView!!.resume()
        try {
            mOutputFile = createVideoOutputFile(this)
            val size = getScreenSize(this)
            mGLView!!.initRecorder(mOutputFile!!, size.x, size.y, null, null)
        } catch (ioex: IOException) {
            Log.e(ContentValues.TAG, "Couldn't re-init recording", ioex)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mIsRecording) {
            mGLView!!.stopRecording()

            val contentUri = FileProvider.getUriForFile(this, "com.example.robocam.fileprovider", mOutputFile!!)

            share(contentUri)
            mIsRecording = false
            mOutputFile = createVideoOutputFile(this)

            try {
                val screenWidth = mGLView!!.width
                val screenHeight = mGLView!!.height
                mGLView!!.initRecorder(mOutputFile!!, screenWidth, screenHeight, null, null)
            } catch (ioex: IOException) {
                Log.e(ContentValues.TAG, "Couldn't re-init recording", ioex)
            }
            item.setTitle("Record")
        } else {
            checkPermission()
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

    private fun checkPermission(){
        val permissions = Permissions(this,
            arrayListOf(Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            23)
        permissions.checkPermissions()
    }
}


@Composable
fun OpenGLScreen() {
    val context = LocalContext.current as MainActivity
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mView.let {
                   MyGLSurfaceView(context).also { glView ->
                       mGLView = glView // Store reference to mGLView
                   }
               }
            },
            update = { view->
                mView = view
                getData(context)
            }
        )

        Text(modifier = Modifier.clickable {
            showDialog(context)
        }, text = "Hello", color = Color.White, fontSize = 34.sp)
    }
}

private fun showDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Dialog Title")
        .setMessage("This is a dialog message.")
        .setPositiveButton(android.R.string.ok, null)
        .show()
}

fun getData(context: Context) {
    mGLView?.resume()
    try {
        mOutputFile = createVideoOutputFile(context)
        val size = getScreenSize(context)
        mGLView?.initRecorder(mOutputFile!!, size.x, size.y, null, null)
    } catch (ioex: IOException) {
        Log.e(ContentValues.TAG, "Couldn't re-init recording", ioex)
    }
}

private fun getScreenSize(context: Context): Point {
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


private fun createVideoOutputFile(context: Context): File? {
    var tempFile: File? = null
    val filename = Date().time.toString() + ""
    var filesDir = ""

    try {
        filesDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path.toString()

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

@Preview
@Composable
fun JetStickUI(modifier: Modifier = Modifier, viewModel: MainViewModel){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var joystickCoordinates by remember { mutableStateOf("X: 0, Y: 0") }

        JoyStickController() { x, y ->
            // joystickCoordinates = coordinates // Update the coordinates in the parent
            Log.d("TAG", "JetStickUI: $x, $y")
        }

        /*   JoyStick(
               Modifier.padding(30.dp),
               size = 150.dp,
               dotSize = 70.dp,
               viewModel = viewModel
           ) { x: Float, y: Float ->
               viewModel.setCoordinates(x,y)
               Log.d("TAG", "JoyStick Camera: $x, $y")
           }*/
    }
}





