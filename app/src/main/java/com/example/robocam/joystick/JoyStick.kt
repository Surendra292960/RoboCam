package com.example.robocam.joystick

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun JoyStick(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    dotSize: Dp = 50.dp,
    moved: (x: Float, y: Float) -> Unit = { _, _ -> }
) {

    val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
    val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
    val centerY = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }

    var offsetX by remember { mutableFloatStateOf(centerX) }
    var offsetY by remember { mutableFloatStateOf(centerY) }

    var radius by remember { mutableFloatStateOf(0f) }
    var theta by remember { mutableFloatStateOf(0f) }

    var positionX by remember { mutableFloatStateOf(0f) }
    var positionY by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier
        .fillMaxHeight()
        .fillMaxWidth(0.45f)
        .background(Color.Transparent)
        .pointerInput(Unit) {
            detectDragGestures(onDragEnd = {
                offsetX = centerX
                offsetY = centerY
                radius = 0f
                theta = 0f
                positionX = 0f
                positionY = 0f
            }) { pointerInputChange: PointerInputChange, offset: Offset ->
                val x = offsetX + offset.x - centerX
                val y = offsetY + offset.y - centerY

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
                }
            }
        }
       ) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .align(Alignment.Center)
                .background(Color.LightGray)
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
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .size(dotSize)
                    .onGloballyPositioned { coordinates ->
                        moved(
                            (coordinates.positionInParent().x - centerX) / maxRadius * 3,
                            (coordinates.positionInParent().y - centerY) / maxRadius * 3
                        )
                    }
            )
        }
    }
}

private fun polarToCartesian(radius: Float, theta: Float): Pair<Float, Float> =
    Pair(radius * cos(theta), radius * sin(theta))