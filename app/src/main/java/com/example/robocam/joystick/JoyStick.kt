package com.example.robocam.joystick

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Returns the absolute value of the given number.
 * @param size Joystick size
 * @param dotSize Joystick Dot size
 * @param backgroundImage Joystick Image Drawable
 * @param dotImage Joystick Dot Image Drawable*/

/*@Composable
fun JoyStick(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    dotSize: Dp = 30.dp,
    backgroundImage: Int = R.drawable.joystick_background_1,
    dotImage: Int = R.drawable.joystick_dot_1,
    viewModel: MainViewModel,
    moved: (x: Float, y: Float) -> Unit = { _, _ -> }
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.LightGray)
            .size(size)
    ) {

        var availableCoordinates by remember { mutableStateOf(Pair(0f, 0f)) }

        var joystickOffset by remember { mutableStateOf(Offset.Zero) }

        val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
        val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
        val centerY = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }

        var offsetX by remember { mutableFloatStateOf(centerX) }
        var offsetY by remember { mutableFloatStateOf(centerY) }

        var radius by remember { mutableFloatStateOf(0f) }
        var theta by remember { mutableFloatStateOf(0f) }

        var positionX by remember { mutableFloatStateOf(0f) }
        var positionY by remember { mutableFloatStateOf(0f) }
        val view = LocalView.current
        var isDragging by remember { mutableStateOf(false) }

        Image(
            painterResource(id = backgroundImage),
            "JoyStickBackground",
            modifier = Modifier.size(size),
        )

       // Log.d("TAG", "JoyStick coordinatesData: $coordinates")
        Log.d("TAG", "JoyStick maxRadius: $maxRadius")

        Image(
            painterResource(id = dotImage),
            "JoyStickDot",
            modifier = Modifier
                .offset {
                    IntOffset(
                        (positionX + centerX).roundToInt(),
                        (positionY + centerY).roundToInt()
                    )
                }
                .size(dotSize)
                .pointerInput(Unit) {
                    Log.d("TAG", "JoyStick: centerXY $centerX, $centerY")
                    detectDragGestures(
                        onDragEnd = {
                            offsetX = centerX
                            offsetY = centerY
                            radius = 0f
                            theta = 0f
                            positionX = 0f
                            positionY = 0f
                            isDragging = false
                            moved(0f, 0f) // Stop movement
                        },
                        onDragCancel = {
                            offsetX = centerX
                            offsetY = centerY
                            radius = 0f
                            theta = 0f
                            positionX = 0f
                            positionY = 0f
                            isDragging = false
                            moved(0f, 0f) // Stop movement
                        }) { pointerInputChange: PointerInputChange, offset: Offset ->
                        isDragging = true
                        Log.d("TAG", "JoyStick: Offset ${offset.x}, ${offset.y}")
                        val x = offsetX + offset.x - centerX
                        val y = offsetY + offset.y - centerY

                        Log.d("TAG", "JoyStick: XY $x, $y")

                        pointerInputChange.consume()

                        theta = if (x >= 0 && y >= 0) {
                            atan(y / x)
                        } else if (x < 0 && y >= 0) {
                            (Math.PI).toFloat() + atan(y / x)
                        } else if (x < 0 && y < 0) {
                            -(Math.PI).toFloat() + atan(y / x)
                        } else {
                            atan(y / x)
                        }

                        radius = sqrt((x.pow(2)) + (y.pow(2)))

                        offsetX += offset.x
                        offsetY += offset.y

                        if (radius > maxRadius) {
                            polarToCartesian(maxRadius, theta)
                        } else {
                            polarToCartesian(radius, theta)
                        }.apply {
                            positionX = first
                            positionY = second

                            Log.d("TAG", "JoyStick positionXY: $offsetX, $offsetY")
                        }

                        // Calculate the new offset
                        joystickOffset = Offset(
                            x = offsetX + offset.x - centerX,
                            y = offsetY + offset.y - centerY
                        )

                        if (radius > 0) {
                            Log.d("TAG", "JoyStick radius cancel first: $radius")
                            CoroutineScope(IO).launch {
                                if (isDragging) {
                                    this.launch {
                                        while (isDragging) {
                                            delay(10)
                                            if (radius == 0f) {
                                                this.cancel()
                                                isDragging = false
                                                Log.d("TAG", "JoyStick radius cancel second: $radius")
                                            }
                                            moved(availableCoordinates.first, availableCoordinates.second)
                                            Log.d("TAG", "JoyStick radius: $radius")
                                        }
                                    }
                                } else {
                                    this.cancel()
                                    Log.d("TAG", "JoyStick radius cancel third: $radius")
                                }
                            }
                        }
                    }
                }
                .onGloballyPositioned { coordinates ->
                    availableCoordinates = Pair((coordinates.positionInParent().x - centerX) / maxRadius, (coordinates.positionInParent().y - centerY) / maxRadius)
                    //moved(availableCoordinates.first, availableCoordinates.second)
                    *//*moved(
                        (coordinates.positionInParent().x - centerX) / maxRadius,
                        (coordinates.positionInParent().y - centerY) / maxRadius
                    )*//*
                },
        )
    }
}


private fun polarToCartesian(radius: Float, theta: Float): Pair<Float, Float> =
    Pair(radius * cos(theta), radius * sin(theta))*/


@Composable
fun Joystick() {
    var joystickOffset by remember { mutableStateOf(Offset(150f, 150f)) } // Start at center
    var coordinates by remember { mutableStateOf("X: 0, Y: 0") }
    val circleRadius = 100f
    val joystickRadius = 20f // Radius of the joystick itself
    val center = Offset(150f, 150f)

    Box(
        modifier = Modifier
            .size(300.dp)
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = {
                    joystickOffset = center
                    coordinates = "X: 0, Y: 0"
                })
                { pointerInputChange:PointerInputChange, offset:Offset ->
                    pointerInputChange.consume()
                    val newOffset = joystickOffset + offset

                    // Calculate distance from center
                    val distance = center.distance(newOffset)

                    // Ensure the joystick remains within the circle boundaries
                    if (distance <= circleRadius - joystickRadius) {
                        joystickOffset = newOffset
                    } else {
                        // Scale the newOffset to the circle's boundary minus joystick radius
                        val direction = (newOffset - center).normalize()
                        joystickOffset = center + direction * (circleRadius - joystickRadius)
                    }

                    // Update coordinates
                    coordinates = "X: ${joystickOffset.x.toInt()}, Y: ${joystickOffset.y.toInt()}"
                }
            }
    ) {
        // Draw the circle and joystick
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Blue,
                radius = circleRadius,
                center = center
            )
            drawCircle(
                color = Color.Red,
                radius = 20f,
                center = joystickOffset
            )
        }

        // Display coordinates
        Text(
            text = coordinates,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}

// Extension function to calculate distance between two offsets
fun Offset.distance(offset: Offset): Float {
    return sqrt((x - offset.x).pow(2) + (y - offset.y).pow(2))
}

// Extension function to normalize an Offset
fun Offset.normalize(): Offset {
    val length = sqrt(x.pow(2) + y.pow(2))
    return if (length != 0f) Offset(x / length, y / length) else Offset(0f, 0f)
}



