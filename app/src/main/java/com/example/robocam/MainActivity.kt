package com.example.robocam
import android.Manifest
import android.content.Intent
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.robocam.joystick.JoyStick
import com.example.robocam.joystick.JoyStickController
import com.example.robocam.opengl.MyCamera
import com.example.robocam.opengl.Permissions
import com.example.robocam.opengl.robo_cam.MyGLSurfaceView
import com.example.robocam.opengl.robo_cam.TextureActivity

class MainActivity : ComponentActivity() {
    private var glSurfaceView: GLSurfaceView? = null
    private var mCamera: MyCamera? = null
    private var mSurface: SurfaceTexture? = null
    private val viewModel:MainViewModel by viewModels()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCamera = MyCamera()
        glSurfaceView = MyGLSurfaceView(this, mCamera!!, true)
        setContentView(glSurfaceView)

        setContent {
            Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                addAlbum()
                var direction by remember { mutableStateOf("Idle") }
                CustomView(mCamera!!).also {
                    Box(modifier = Modifier
                        .fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        JetStickUI(modifier = Modifier.align(Alignment.CenterStart), viewModel)
                       /* JoyStick(viewModel = MainViewModel()){ x,y->
                            Log.d("TAG", "joystick Wheel: $x, $y")
                        }*/
                    }
                }
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
    val mContext = LocalContext.current as MainActivity
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Creates view
            MyGLSurfaceView(context, mCamera, true).apply {

            }

        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary
        }
    )
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



