package com.example.robocam.joystick

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.pow
import kotlin.math.sqrt


var jobWheel: Job? = null
var innerCircleColor:Color = Color.Gray

var distance:Float = 0f

@Preview
@Composable
fun JoyStickControllerUIPreview(){
    JoyStickController()
}

@Composable
fun JoyStickController(
    modifier: Modifier=Modifier,
    onCoordinatesChange: (x: Float, y: Float) -> Unit = { _, _ -> }) {
    var coordinates by remember { mutableStateOf(Offset(0f,0f)) }
    var joystickOffset by remember { mutableStateOf(Offset(150f, 150f)) } // Start at center
    val circleRadius = 180f
    val joystickRadius = 70f // Radius of the joystick itself
    val center = Offset(150f, 150f)
    var isDragging by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .fillMaxHeight()
        .fillMaxWidth(0.45f)
        .background(Color.Transparent)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    distance = 0f
                    isDragging = false
                    joystickOffset = center
                    coordinates = Offset(0f, 0f)
                    onCoordinatesChange(coordinates.x, coordinates.y)  // Reset coordinates
                },
                onDragCancel = {
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
                val normalizedCoordinates = calculateNormalizedCoordinates(joystickOffset, center, circleRadius - joystickRadius)
                coordinates = Offset(normalizedCoordinates.first, normalizedCoordinates.second)
                onCoordinatesChange(coordinates.x, coordinates.y)  // Invoke the callback
            }
        }) {

        Box(
            modifier = Modifier
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
                    val angle = atan2(joystickOffset.y - center.y, joystickOffset.x - center.x) * (180 / PI.toFloat())

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
                }
        )

        // Display coordinates
        Log.d("TAG", "JoystickHandler Wheels: X = ${coordinates.x}, Y = ${coordinates.y}")
        Text(
            text = "X = ${coordinates.x}, Y = ${coordinates.y}",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            color = Color.White,
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

// Extension function to calculate the distance between two offsets
private fun Offset.getDistance(): Float {
    return sqrt(x * x + y * y)
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
