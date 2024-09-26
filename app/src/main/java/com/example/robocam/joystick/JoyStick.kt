package com.example.robocam.joystick

import android.os.Handler
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.robocam.MainViewModel
import com.example.robocam.R
import kotlin.math.*

/**
 * Returns the absolute value of the given number.
 * @param size Joystick size
 * @param dotSize Joystick Dot size
 * @param backgroundImage Joystick Image Drawable
 * @param dotImage Joystick Dot Image Drawable
 */

private var movementHandler: Handler = Handler()
private var isMoving = false
private var movementRunnable: Runnable? = null

@Composable
fun JoyStick(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    dotSize: Dp = 30.dp,
    backgroundImage: Int = R.drawable.joystick_background_1,
    dotImage: Int = R.drawable.joystick_dot_1,
    viewModel:MainViewModel,
    moved: (x: Float, y: Float) -> Unit = { _, _ -> }
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.LightGray)
            .size(size)
    ) {

        val coordinates = viewModel.coordinates.collectAsState().value

        val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
        val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
        val centerY = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }

        var offsetX by remember { mutableFloatStateOf(centerX) }
        var offsetY by remember { mutableFloatStateOf(centerY) }

        var radius by remember { mutableFloatStateOf(0f) }
        var theta by remember { mutableFloatStateOf(0f) }

        var positionX by remember { mutableFloatStateOf(0f) }
        var positionY by remember { mutableFloatStateOf(0f) }



       /* Image(
            painterResource(id = backgroundImage),
            "JoyStickBackground",
            modifier = Modifier.size(size),
        )*/

        Log.d("TAG", "JoyStick coordinatesData: $coordinates")

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
                    detectDragGestures(onDragEnd = {
                        offsetX = centerX
                        offsetY = centerY
                        radius = 0f
                        theta = 0f
                        positionX = 0f
                        positionY = 0f

                        stopContinuousMovement()
                    }) { pointerInputChange: PointerInputChange, offset: Offset ->
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

                        checkContinuousMovement(x, y, radius)

                        offsetX += offset.x
                        offsetY += offset.y

                        if (radius > maxRadius) {
                            Log.d("TAG", "JoyStick radius: $radius, $maxRadius")
                            polarToCartesian(maxRadius, theta)
                        } else {
                            polarToCartesian(radius, theta)
                        }.apply {
                            positionX = first
                            positionY = second
                        }
                    }
                }
                .onGloballyPositioned { coordinates ->
                    moved(
                        (coordinates.positionInParent().x - centerX) / maxRadius,
                        (coordinates.positionInParent().y - centerY) / maxRadius
                    )
                },
        )
    }
}

private fun polarToCartesian(radius: Float, theta: Float): Pair<Float, Float> = Pair(radius * cos(theta), radius * sin(theta))


private fun checkContinuousMovement(dx: Float, dy: Float, radius: Float) {

    val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

    Log.d("TAG", "JoyStick radius: $distance, ${radius * 0.9}")
    if (distance >= radius * 0.9) { // Near edge
        if (!isMoving) {
            Log.d("TAG", "checkContinuousMovement: Start ")
            startContinuousMovement(dx, dy)
        }
    } else {
        Log.d("TAG", "checkContinuousMovement: Stop ")
        stopContinuousMovement()
    }
}

private fun startContinuousMovement(dx: Float, dy: Float) {
    isMoving = true
    movementRunnable = object : Runnable {
        override fun run() {
            // Perform movement logic here
            // Example: Log.d("Joystick", "Moving: dx: $dx, dy: $dy")

            Log.d("TAG", "startContinuousMovement:Joystick Moving: dx: $dx, dy: $dy")
         /*   getStickPosition().also {
                Log.d("TAG", "startContinuousMovement:Joystick ${it.first} ${it.second}")
            }*/

            movementHandler.postDelayed(this, 100) // Adjust the delay as needed
        }
    }
    movementHandler.post(movementRunnable!!)
}

private fun stopContinuousMovement() {
    isMoving = false
    if (movementRunnable!=null){
        movementHandler.removeCallbacks(movementRunnable!!)
    }
    Log.d("TAG", "stopContinuousMovement: ")
}