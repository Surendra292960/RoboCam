package com.example.robocam

import Utility.getScreenSize
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.robocam.joystick.JoyStickController
import com.example.robocam.joystick.d_pad.JoystickDPad
import com.example.robocam.video_stream.MyGLSurfaceView
import com.example.robocam.video_stream.RecordableSurfaceView

var mView: View? = null
private var mGLView: RecordableSurfaceView? = null
private var mMyGLView: MyGLSurfaceView? = null

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
                OpenGLScreen().also {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
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
        mGLView?.resume()
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
                        mMyGLView = glView
                        mGLView = glView // Store reference to mGLView
                    }
                }

                //JoystickView(context)
            },
            update = { view ->
                mView = view
                mGLView?.resume()
            }
        )

        DisplayIcons(context)
    }
}

@Composable
fun DisplayIcons(context: Context) {
    var isRecording by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val flashAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
    ) {
        Text(
            modifier = Modifier.clickable {
                mMyGLView?.mRenderer?.isSave = true
            },
            text = "Show Dialog",
            color = Color.Cyan, fontSize = 15.sp, fontWeight = FontWeight.Bold
        )

        if (!isRecording) {
            Icon(
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        //startRecording(context)
                        isRecording = true
                    }
                    .align(Alignment.BottomStart),
                painter = painterResource(R.drawable.recording),
                contentDescription = "start recording",
                tint = Color.Cyan
            )
        } else {
            Icon(
                modifier = Modifier
                    .size(30.dp)
                    .scale(flashAnimation)
                    .clickable {
                        // stopRecording()
                        isRecording = false
                    }
                    .align(Alignment.BottomStart),
                painter = painterResource(R.drawable.recording),
                contentDescription = "stop recording",
                tint = Color.Red
            )
        }
    }
}

fun startRecording(context: Context) {
    mGLView?.resume()
    try {
        val size = getScreenSize(context)
        mGLView?.initRecorder(size.x, size.y, 0, null, null)
        mGLView!!.startRecording()
        Log.d("TAG", "Recording Started ")
    } catch (e: Exception) {
        Log.d("TAG", "Recording Start Exception : ${e.message}")
    }
}

fun stopRecording() {
    try {
        mGLView!!.stopRecording()
        Log.d(ContentValues.TAG, "Recording Stopped")
    } catch (e: Exception) {
        Log.d("TAG", "Recording Stopped Exception : ${e.message}")
    }
}


@Preview
@Composable
fun JetStickUI(modifier: Modifier = Modifier, viewModel: MainViewModel) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            JoyStickController(
                modifier = Modifier
                    .padding(end = 10.dp, top = 35.dp, bottom = 45.dp)
            ) { x, y ->
                Log.d("TAG", "JetStickUI ONE: $x, $y")
                viewModel.setJoystickData(LeftJoyStickData(x, y), null)
            }

            JoystickDPad(
                modifier = Modifier
                    .padding(end = 10.dp, top = 35.dp, bottom = 45.dp).weight(0.45f), viewModel = viewModel
            ) { x, y ->
                Log.d("TAG", "JetStickUI Velocity: $x, $y")
                viewModel.setJoystickData(null, RightJoyStickData(x, y))
            }
        }
    }
}






