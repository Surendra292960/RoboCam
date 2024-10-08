package com.example.robocam.joystick

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robocam.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt


var job: Job? = null
var innerCircleColor:Color = Color.Gray

var distance:Float = 0f
@Composable
fun JoyStickController(onCoordinatesChange: (x: Float, y: Float) -> Unit = { _, _ -> }) {
    var coordinates by remember { mutableStateOf(Offset(0f,0f)) }
    var joystickOffset by remember { mutableStateOf(Offset(150f, 150f)) } // Start at center
    val circleRadius = 180f
    val joystickRadius = 70f // Radius of the joystick itself
    val center = Offset(150f, 150f)
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .size(300.dp)
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        cancelJob()
                        isDragging = false
                        joystickOffset = center
                        coordinates = Offset(0f, 0f)
                        onCoordinatesChange(coordinates.x, coordinates.y)  // Reset coordinates
                    },
                    onDragCancel = {
                        cancelJob()
                        isDragging = false
                        joystickOffset = center
                        coordinates = Offset(0f, 0f)
                        onCoordinatesChange(coordinates.x, coordinates.y)  // Reset coordinates
                    }
                ) { change, dragAmount ->
                    isDragging = true
                    change.consume()
                    val newOffset = joystickOffset + dragAmount

                    // Calculate distance from center
                    distance = center.distance(newOffset)

                    // Ensure the joystick remains within the circle boundaries
                    if (distance <= circleRadius - joystickRadius) {
                        Log.d("TAG", "JoyStickController: distance lesser $distance")
                        joystickOffset = newOffset
                        innerCircleColor = Color.Gray
                    } else {
                        Log.d("TAG", "JoyStickController: distance greater $distance")
                        // Change the color of the inner circle when at the edge
                        innerCircleColor = Color.Red
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        // Scale the newOffset to the circle's boundary minus joystick radius
                        val direction = (newOffset - center).normalize()
                        joystickOffset = center + direction * (circleRadius - joystickRadius)
                    }

                    // Calculate normalized coordinates
                    val normalizedCoordinates = calculateNormalizedCoordinates(
                        joystickOffset,
                        center,
                        circleRadius - joystickRadius
                    )

                    if (isDragging) {
                        job = CoroutineScope(IO).launch {
                            Log.d("TAG", "JoyStickController Job start  : ${job!!.isActive}")
                            while (job!!.isActive) {
                                delay(10)
                                if (job!!.isActive) {
                                    coordinates = Offset(
                                        normalizedCoordinates.first,
                                        normalizedCoordinates.second
                                    )
                                    onCoordinatesChange(
                                        coordinates.x,
                                        coordinates.y
                                    )  // Invoke the callback
                                    Log.d("TAG", "JoyStickController Job started alone if : ")
                                } else {
                                    coordinates = Offset(0f, 0f)
                                    onCoordinatesChange(
                                        coordinates.x,
                                        coordinates.y
                                    )  // Invoke the callback
                                    Log.d("TAG", "JoyStickController Job started alone  else : ")
                                }
                            }
                        }
                    }
                }
            }
    ) {
        var image = painterResource(id = R.drawable.joystick_dot_1)
        var icon = painterResource(id = R.drawable.joystick_background_1) // Add your icon resource
        // Draw the circle and joystick
        Canvas(modifier = Modifier
            .fillMaxSize()
            .align(alignment = Alignment.BottomCenter)
            .requiredSize(
                width = 120.dp,
                height = 120.dp,
            )) {
            drawCircle(color = Color.LightGray, radius = circleRadius, center = center)

            drawCircle(color = innerCircleColor, radius = joystickRadius, center = joystickOffset)
        }

        // Display coordinates
        Text(
            text = "X = ${coordinates.x}, Y = ${coordinates.y}",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            color = Color.Black,
            fontSize = 16.sp
        )

        // Reset Button
        Button(
            onClick = {
                joystickOffset = center
                coordinates = Offset(0f, 0f)
                onCoordinatesChange(coordinates.x, coordinates.y) // Reset coordinates
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Reset")
        }
    }
}

fun cancelJob() {
    CoroutineScope(IO).launch {
        innerCircleColor = Color.Gray
        job!!.cancelAndJoin()
        Log.d("TAG", "JoyStickController isActive Job : ${job!!.isActive}")
    }
}

// Extension function to calculate distance between two offsets
fun Offset.distance(other: Offset): Float {
    return sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
}

// Extension function to normalize an Offset
fun Offset.normalize(): Offset {
    val length = sqrt(x.pow(2) + y.pow(2))
    return if (length != 0f) Offset(x / length, y / length) else Offset(0f, 0f)
}

// Function to calculate normalized coordinates based on joystick position
fun calculateNormalizedCoordinates(joystickOffset: Offset, center: Offset, maxDistance: Float): Pair<Float, Float> {
    val dx = joystickOffset.x - center.x
    val dy = joystickOffset.y - center.y

    // Normalize the joystick position to be between -1 and 1
    val normalizedX = (dx / (maxDistance)).coerceIn(-1f, 1f)
    val normalizedY = (dy / (maxDistance)).coerceIn(-1f, 1f)

    return Pair(normalizedX, normalizedY)
}


