package com.example.robocam.joystick

import android.R.color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
import kotlin.math.pow
import kotlin.math.sqrt


var job: Job? = null
var innerCircleColor:Color = Color.Gray

var distance:Float = 0f

@Composable
fun JoyStickController(onCoordinatesChange: (x: Float, y: Float) -> Unit = { _, _ -> }) {
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
        .size(300.dp)
        .background(Color.White)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    cancelJob()
                    distance = 0f
                    isDragging = false
                    joystickOffset = center
                    coordinates = Offset(0f, 0f)
                    onCoordinatesChange(coordinates.x, coordinates.y)  // Reset coordinates
                },
                onDragCancel = {
                    cancelJob()
                    distance = 0f
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
        }) {

        // Draw the circle and joystick
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(alignment = Alignment.BottomCenter)
                .requiredSize(
                    width = 120.dp,
                    height = 120.dp,
                )
                .drawBehind {
                    // Draw the outer circle
                    drawCircle(color = Color.LightGray, radius = circleRadius, center = center)

                    // Draw the inner joystick circle
                    drawCircle(color = Color.Gray, radius = joystickRadius, center = joystickOffset)

                    // Calculate the distance from the center to the joystick
                    val distance = (joystickOffset - center).getDistance()

                    val angle = atan2(joystickOffset.y - center.y, joystickOffset.x - center.x) * (180 / PI.toFloat())
                    val startAngle = angle - 30f
                    val sweepAngle = 60f

                    //val path = Path().apply { addArc(Rect(center.x - circleRadius, center.y - circleRadius, center.x + circleRadius, center.y + circleRadius), startAngle, sweepAngle) }

                    val path = Path().apply {
                        // Move to the starting point
                        //moveTo(center.x, center.y)

                        // Create a semi-circle
                        arcTo(
                            rect = Rect(center.x - circleRadius, center.y - circleRadius, center.x + circleRadius, center.y + circleRadius),
                                    //Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius),
                            startAngleDegrees = startAngle,
                            sweepAngleDegrees = sweepAngle,
                            forceMoveTo = false
                        )
                        close() // Close the path
                    }
                    // Draw the arc only when the joystick is at the edge
                    if (isDragging) {
                        Log.d("TAG", "JoyStickController drawBehind : ")
                        //haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        with(painter) {
                            drawPath(
                                path = path,
                                color = Color.Cyan,
                                style = Stroke(
                                    width = 25f,
                                    join = StrokeJoin.Miter,
                                    pathEffect = PathEffect.cornerPathEffect(joystickRadius-20),
                                    cap = StrokeCap.Square
                                ),
                            )
                            draw(
                                size = painter.intrinsicSize,
                                alpha = .5f, // optional
                                colorFilter = tint // optional
                            )
                        }
                     /*   drawPath(
                            path = path,
                            color = Color.Cyan,
                            style = Stroke(
                                width = 25f,
                                join = StrokeJoin.Miter,
                                pathEffect = PathEffect.cornerPathEffect(joystickRadius-20),
                                cap = StrokeCap.Square
                            ),
                        )*/
                    }
                }
        )

        // Display coordinates
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


// Extension function to calculate distance
fun Offset.getDistance(): Float {
    return sqrt(x * x + y * y)
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


@Composable
fun ArchButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val path = Path().apply {
                // Define the arch's shape using moveTo, lineTo, and arcTo
                moveTo(size.width / 4f, size.height)
                lineTo(size.width / 2f, size.height / 2f)
                arcTo(
                    Rect(
                        size.width / 4f,
                        size.height / 2f,
                        size.width * 3f / 4f,
                        size.height
                    ),
                    0f,
                    180f,
                    true
                )
                lineTo(size.width * 3f / 4f, size.height)
            }

            drawPath(
                path = path,
                color = Color.Yellow,
                //strokeWidth = 5f,
                style = Stroke(width = 5f)
            )
        }
    }
}
