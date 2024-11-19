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
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt


var jobCamera: Job? = null

@Preview
@Composable
fun CameraControllerUIPreview(){
    CameraController()
}

@Composable
fun CameraController(coordinatesChange: (x: Float, y: Float) -> Unit = { _, _ -> }) {
    var coordinates by remember { mutableStateOf(Offset(0f,0f)) }
    var joystickOffset by remember { mutableStateOf(Offset(150f, 150f)) } // Start at center
    // Maybe store this in a static field?
    val SCALE: Float = LocalContext.current.resources.displayMetrics.density
    // Convert dips to pixels
    val circleRadiusDips =80f
    //val circleRadius = (circleRadiusDips * SCALE + 0.5f) // 0.5f for rounding
    val circleRadius = 180f
    // Convert dips to pixels
    val joystickRadiusDips =30f
    // val joystickRadius = (joystickRadiusDips * SCALE + 0.5f) // 0.5f for rounding
    val joystickRadius = 70f // Radius of the joystick itself
    val center = Offset(150f, 150f)
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    val painter = rememberVectorPainter(image = Icons.Default.Add)
    val tint = remember { ColorFilter.tint(Color.Red) }

    Box(modifier = Modifier
        .size(400.dp)
        .background(Color.Transparent)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    cancelCameraJob()
                    distance = 0f
                    isDragging = false
                    joystickOffset = center
                    coordinates = Offset(0f, 0f)
                    coordinatesChange(coordinates.x, coordinates.y)  // Reset coordinates
                },
                onDragCancel = {
                    cancelCameraJob()
                    distance = 0f
                    isDragging = false
                    joystickOffset = center
                    coordinates = Offset(0f, 0f)
                    coordinatesChange(coordinates.x, coordinates.y)  // Reset coordinates
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

                    // Scale the newOffset to the circle's boundary minus joystick radius
                    val direction = (newOffset - center).normalize()
                    joystickOffset = center + direction * (circleRadius - joystickRadius)
                }

                // Calculate normalized coordinates
                val normalizedCoordinates = calculateNormalizedCoordinates(joystickOffset, center, circleRadius - joystickRadius)

                if (isDragging) {
                    jobCamera = CoroutineScope(IO).launch {
                        Log.d("TAG", "JoyStickController Job start  : ${jobCamera?.isActive}")
                        if (jobCamera?.isActive == true) {
                            coordinates = Offset(normalizedCoordinates.first, normalizedCoordinates.second)
                            coordinatesChange(coordinates.x, coordinates.y)  // Invoke the callback
                            Log.d("TAG", "JoyStickController Job started alone if : ")
                        } else {
                            coordinates = Offset(0f, 0f)
                            coordinatesChange(coordinates.x, coordinates.y)  // Invoke the callback
                            Log.d("TAG", "JoyStickController Job started alone  else : ")
                        }
                    }
                }
            }
        }) {

        Box(modifier = Modifier
            .fillMaxSize()
            .align(alignment = Alignment.BottomCenter)
            .requiredSize(120.dp)
            .drawBehind {

                // Define the maximum radius for the yellow circle's movement
                val maxYellowRadius = 10.dp.toPx() // Adjust this value for desired effect

                // Calculate the vector from the center to the joystick offset
                val joystickVector = joystickOffset - center

                // Clamp the joystick vector's length to the maxYellowRadius
                val clampedJoystickVector = joystickVector.clampLength(maxYellowRadius)

                // Calculate the outer circle's position based on the clamped joystick vector
                val outerCircleOffset = center + clampedJoystickVector

                // Draw the outer joystick circle at the calculated offset
                drawCircle(color = Color.Cyan, radius = circleRadius, center = outerCircleOffset)

                // Draw the medium joystick circle at the center
                drawCircle(color = Color.LightGray, radius = circleRadius, center = center)

                // Draw the inner joystick circle at the joystick offset
                drawCircle(color = Color.Gray, radius = joystickRadius, center = joystickOffset)

                // Calculate the angle for the yellow path
                val angle = atan2(
                    joystickOffset.y - center.y,
                    joystickOffset.x - center.x
                ) * (180 / PI.toFloat())

                // Calculate the position for the yellow arc based on the outer circle's position
                val yellowArcCenter = outerCircleOffset

                val startAngle = angle - 2f
                val sweepAngle = 4f

                val path = Path().apply {
                    addArc(
                        Rect(
                            yellowArcCenter.x - circleRadius,
                            yellowArcCenter.y - circleRadius,
                            yellowArcCenter.x + circleRadius,
                            yellowArcCenter.y + circleRadius
                        ),
                        startAngle,
                        sweepAngle
                    )
                }

                drawPath(
                    path = path,
                    color = Color.Cyan,
                    style = Stroke(
                        width = 20f,
                        pathEffect = PathEffect.cornerPathEffect(joystickRadius),
                        join = StrokeJoin.Miter,
                        cap = StrokeCap.Square
                    ),
                )
            })

        // Display coordinates
        Log.d("TAG", "JoystickHandler Camera : X = ${coordinates.x}, Y = ${coordinates.y}")
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
        Log.d("TAG", "CameraController isActive Job : ${jobCamera!!.isActive}")
    }
}