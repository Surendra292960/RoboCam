package com.example.robocam.joystick

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2

var jobCamera: Job? = null
@Preview
@Composable
fun CameraControllerUIPreview(){
    CameraController()
}

@Composable
fun CameraController(coordinatesChange: (x: Float, y: Float) -> Unit = { _, _ -> }) {
    var coordinates by remember { mutableStateOf(Offset(0f, 0f)) }
    var joystickOffset by remember { mutableStateOf(Offset(150f, 150f)) } // Start at center
    val center = Offset(150f, 150f)
    var lastDragPosition by remember { mutableStateOf(joystickOffset) }
    var isDragging by remember { mutableStateOf(false) }
    var isFingerMoving by remember { mutableStateOf(false) } // State to track if the finger is moving
    val circleRadius = 180f
    val joystickRadius = 70f // Radius of the joystick itself
    val coroutineScope = rememberCoroutineScope()
    var stableState by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .size(400.dp)
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        cancelCameraJob()
                        isDragging = false
                        stableState = true
                        isFingerMoving = false
                        joystickOffset = center
                        coordinates = Offset(0f, 0f)
                        coordinatesChange(coordinates.x, coordinates.y) // Reset coordinates
                    },
                    onDragCancel = {
                        cancelCameraJob()
                        isDragging = false
                        stableState = true
                        isFingerMoving = false
                        joystickOffset = center
                        coordinates = Offset(0f, 0f)
                        coordinatesChange(coordinates.x, coordinates.y) // Reset coordinates
                    }
                ) { change, dragAmount ->
                    /// isDragging = true
                    change.consume()
                    val newOffset = joystickOffset + dragAmount

                    // Detect start and stop of finger movement
                    val distanceFromLastDrag = lastDragPosition.distance(newOffset)
                    if (distanceFromLastDrag > 5f) { // Finger moving threshold
                        if (!isFingerMoving) {
                            stableState = false
                            Log.d("CameraController", "Finger started moving!")
                        }
                        isFingerMoving = true
                    } else {
                        if (isFingerMoving) {
                            coroutineScope.launch {
                                stableState = true
                                cancelCameraJob()
                                coordinates = Offset(0f, 0f)
                                coordinatesChange(coordinates.x, coordinates.y)
                                Log.d("CameraController", "Finger stopped moving!")
                            }
                        }
                        isFingerMoving = false
                    }

                    lastDragPosition = newOffset

                    // Limit joystick offset within boundaries
                    val distance = center.distance(newOffset)
                    if (distance <= circleRadius - joystickRadius) {
                        joystickOffset = newOffset
                    } else {
                        val direction = (newOffset - center).normalize()
                        joystickOffset = center + direction * (circleRadius - joystickRadius)
                    }

                    // Calculate normalized coordinates
                    val normalizedCoordinates = calculateNormalizedCoordinates(joystickOffset, center, circleRadius - joystickRadius)
                    if (isFingerMoving) {
                        jobCamera = CoroutineScope(IO).launch {
                            Log.d("CameraController", "JoyStickController Job start  : ${jobCamera?.isActive}")
                            while (jobCamera?.isActive == true) {
                                delay(10)
                                if (jobCamera!!.isActive) {
                                    coordinates = Offset(normalizedCoordinates.first, normalizedCoordinates.second)
                                    coordinatesChange(coordinates.x, coordinates.y)  // Invoke the callback
                                    Log.d("CameraController", "JoyStickController Job started alone if : ")
                                }
                            }
                        }
                    }else if(stableState){
                        cancelCameraJob()
                        coordinates = Offset(0f, 0f)
                        coordinatesChange(coordinates.x, coordinates.y)  // Invoke the callback
                        Log.d("CameraController", "JoyStickController Job started alone  else super : ")
                    }
                }
            }
    ) {
        // UI rendering and debug text
        Text(
            text = "X = ${coordinates.x}, Y = ${coordinates.y}",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}


// Extension function to clamp a vector's length
private fun Offset.clampLength(maxLength: Float): Offset {
    val length = getDistance()
    return if (length > maxLength) {
        val ratio = maxLength / length
        Offset(x * ratio, y * ratio)
    } else {
        this
    }
}

fun cancelCameraJob() {
    CoroutineScope(IO).launch {
        innerCircleColor = Color.Gray
        jobCamera?.cancelAndJoin()
        Log.d("TAG", "CameraController isActive Job : ${jobCamera?.isActive}")
    }
}