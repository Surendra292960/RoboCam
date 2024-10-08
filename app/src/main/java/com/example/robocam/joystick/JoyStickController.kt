package com.example.robocam.joystick

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robocam.R
import kotlin.math.pow
import kotlin.math.sqrt


@Composable
fun JoyStickController(onCoordinatesChange: (String) -> Unit) {
    var coordinates by remember { mutableStateOf("X: 0, Y: 0") }
    var joystickOffset by remember { mutableStateOf(Offset(150f, 150f)) } // Start at center
    val circleRadius = 180f
    val joystickRadius = 70f // Radius of the joystick itself
    val center = Offset(150f, 150f)
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Creating animation
    val animateFloat = remember { Animatable(0f) }
    val startAnim = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(300.dp)
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        joystickOffset = center
                        onCoordinatesChange("X: 0, Y: 0") // Reset coordinates
                    },
                    onDragCancel = {
                        joystickOffset = center
                        onCoordinatesChange("X: 0, Y: 0") // Reset coordinates
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newOffset = joystickOffset + dragAmount

                    // Calculate distance from center
                    val distance = center.distance(newOffset)

                    // Ensure the joystick remains within the circle boundaries
                    if (distance <= circleRadius - joystickRadius) {
                        Log.d("TAG", "JoyStickController: distance lesser $distance")
                        joystickOffset = newOffset
                    } else {
                        Log.d("TAG", "JoyStickController: distance greater $distance")
                        startAnim.value = true
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
                    coordinates =
                        "X: ${normalizedCoordinates.first}, Y: ${normalizedCoordinates.second}"
                    onCoordinatesChange(coordinates) // Invoke the callback
                }
            }
    ) {
        if (startAnim.value){
            LaunchedEffect(key1 = startAnim.value) {
                animateFloat.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1, easing = LinearEasing))
            }
        }

        // Draw the circle and joystick
        Canvas(modifier = Modifier
            .fillMaxSize()
            .align(alignment = Alignment.BottomCenter)
            .requiredSize(
                width = 120.dp,
                height = 120.dp,
            )) {
            drawCircle(color = Color.LightGray, radius = circleRadius, center = center)

            drawCircle(color = Color.Gray, radius = joystickRadius, center = joystickOffset).apply {
                ImageVector
            }
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

        // Reset Button
        Button(
            onClick = {
                joystickOffset = center
                onCoordinatesChange("X: 0, Y: 0") // Reset coordinates
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Reset")
        }
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

