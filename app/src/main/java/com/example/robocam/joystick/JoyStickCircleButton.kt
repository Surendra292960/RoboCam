package com.example.robocam.joystick

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.robocam.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Returns the absolute value of the given number.
 * @param size Joystick size
 * @param dotSize Joystick Dot size
 * @param backgroundImage Joystick Image Drawable
 * @param dotImage Joystick Dot Image Drawable*/

@Preview
@Composable
fun PreviewScreen() {
    JoyStickButton(viewModel = MainViewModel())
}

@Composable
fun JoyStickButton(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    dotSize: Dp = 70.dp,
    viewModel: MainViewModel,
    moved: (x: Float, y: Float) -> Unit = { _, _ -> }
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

    Box(modifier = modifier
        .size(400.dp)
        .background(Color.White)
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
                }) {
                pointerInputChange: PointerInputChange, offset: Offset ->
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

                    Log.d("TAG", "JoyStick offsetXY: $offsetX, $offsetY")
                    Log.d("TAG", "JoyStick positionXY: $positionX, $positionY")
                }


                Log.d("TAG", "JoyStick joystickOffset: ${positionX + centerX}, ${positionY + centerY}")

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
                                    moved(
                                        availableCoordinates.first,
                                        availableCoordinates.second
                                    )
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
        }) {

        Box(
            modifier = modifier
                .clip(CircleShape)
                .align(Alignment.Center)
                .background(Color.LightGray)
                .size(size + 70.dp)) {
            Box(
                modifier = modifier
                    .align(Alignment.Center)
                    .background(Color.Transparent)
                    .size(size)
            ) {
                Box(modifier = Modifier
                    .offset {
                        IntOffset(
                            (positionX + centerX).roundToInt(),
                            (positionY + centerY).roundToInt()
                        )
                    }
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .onGloballyPositioned { coordinates ->
                        availableCoordinates = Pair(
                            (coordinates.positionInParent().x - centerX) / maxRadius,
                            (coordinates.positionInParent().y - centerY) / maxRadius
                        )
                        //moved(availableCoordinates.first, availableCoordinates.second)
                        moved(
                            (coordinates.positionInParent().x - centerX) / maxRadius,
                            (coordinates.positionInParent().y - centerY) / maxRadius
                        )
                    }
                )
            }
        }
    }
}


private fun polarToCartesian(radius: Float, theta: Float): Pair<Float, Float> =
    Pair(radius * cos(theta), radius * sin(theta))





