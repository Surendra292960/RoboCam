package com.example.robocam.joystick.d_pad

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.robocam.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/*@Composable
fun JoystickDPad(
    modifier: Modifier = Modifier,
    onCoordinatesChanged: (Offset) -> Unit, // Callback to provide joystick coordinates
) {
    // State to keep track of the joystick's position
    var touchPosition by remember { mutableFloatStateOf(Offset.Zero) }
    val radius = 70.dp // Outer circle's radius

    Box(modifier = modifier
        .fillMaxHeight()
        .fillMaxWidth(0.45f)
        .background(Color.Transparent)
        .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        touchPosition = Offset.Zero // Reset to center on interaction start
                        onCoordinatesChanged(touchPosition)
                    },
                    onDrag = { change, dragAmount ->
                        touchPosition += dragAmount

                        // Limit the touchPosition within the joystick radius
                        val maxRadius = radius.toPx()/2
                        val constrainedOffset = Offset(
                            x = touchPosition.x.coerceIn(-maxRadius, maxRadius),
                            y = touchPosition.y.coerceIn(-maxRadius, maxRadius)
                        )

                        touchPosition = constrainedOffset
                        onCoordinatesChanged(touchPosition) // Pass coordinates to the callback
                        change.consume() // Consume the gesture
                    },
                    onDragEnd = {
                        touchPosition = Offset.Zero // Reset joystick to center on release
                        onCoordinatesChanged(touchPosition)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer joystick Circle
        Box(
            modifier = Modifier
                .size(radius * 2) // Diameter of the joystick
                .background(Color.LightGray, shape = CircleShape)
        ){
            // Inner joystick handle
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset {
                        IntOffset(
                            x = touchPosition.x.toInt(),
                            y = touchPosition.y.toInt()
                        )
                    }
                    .size(50.dp) // Size of the joystick handle
                    .background(Color.Gray, shape = CircleShape)
            )
        }

        // Display coordinates
        Log.d("TAG", "JoystickHandler Wheels: X = ${touchPosition.x}, Y = ${touchPosition.y}")
        Text(
            text = "X = ${touchPosition.x}, Y = ${touchPosition.y}",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            color = Color.White,
            fontSize = 16.sp
        )
    }
}*/

@Composable
fun JoystickDPad(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    dotSize: Dp = 50.dp,
    viewModel: MainViewModel,
    velocity: (vx: Float, vy: Float) -> Unit = { _, _ -> }
) {
    val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
    val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
    val centerY = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }

    var offsetX by remember { mutableFloatStateOf(centerX) }
    var offsetY by remember { mutableFloatStateOf(centerY) }

    var positionX by remember { mutableFloatStateOf(0f) }
    var positionY by remember { mutableFloatStateOf(0f) }

    // For velocity calculation
    var lastNormalizedX by remember { mutableFloatStateOf(0f) }
    var lastNormalizedY by remember { mutableFloatStateOf(0f) }
    var lastTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.45f)
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Reset joystick and velocity on drag end
                        offsetX = centerX
                        offsetY = centerY
                        positionX = 0f
                        positionY = 0f
                        velocity(0.0f, 0.0f)
                    }
                ) { change, dragAmount ->
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = (currentTime - lastTime).toFloat() / 100f // in seconds

                    // Update raw offsets
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y

                    val rawX = offsetX - centerX
                    val rawY = offsetY - centerY

                    // Clamp radius to maxRadius and convert to polar
                    val radius = min(sqrt(rawX.pow(2) + rawY.pow(2)), maxRadius)
                    val theta = atan2(rawY, rawX)
                    val (clampedX, clampedY) = polarToCartesian(radius, theta)

                    // Normalize position to the range [-1, 1]
                    val normalizedX = (clampedX / maxRadius).coerceIn(-0.99f, 0.99f)
                    val normalizedY = (clampedY / maxRadius).coerceIn(-0.99f, 0.99f)

                    // Update position for display
                    positionX = normalizedX * maxRadius
                    positionY = normalizedY * maxRadius

                    // Calculate velocity based on normalized positions
                    if (elapsedTime > 0) {
                        val vx = ((normalizedX - lastNormalizedX) / elapsedTime).coerceIn(-0.99f, 0.99f)
                        val vy = ((normalizedY - lastNormalizedY) / elapsedTime).coerceIn(-0.99f, 0.99f)
                        velocity(vx, vy)

                        // Reset velocity after a delay
                        viewModel.viewModelScope.launch {
                            delay(300)
                            velocity(0.0f, 0.0f)
                        }
                    }

                    // Update last position and time
                    lastNormalizedX = normalizedX
                    lastNormalizedY = normalizedY
                    lastTime = currentTime

                    // Consume gesture
                    change.consume()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.LightGray, shape = CircleShape)
                .size(size)
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (positionX + centerX).roundToInt(),
                            (positionY + centerY).roundToInt()
                        )
                    }
                    .background(Color.Gray, shape = CircleShape)
                    .size(dotSize)
            )
        }
    }
}

private fun polarToCartesian(radius: Float, theta: Float): Pair<Float, Float> =
    Pair(radius * cos(theta), radius * sin(theta))

