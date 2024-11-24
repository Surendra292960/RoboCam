package com.example.robocam.joystick.d_pad
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JoystickDPad(
    modifier: Modifier = Modifier,
    onCoordinatesChanged: (Offset) -> Unit, // Callback to provide joystick coordinates
) {
    // State to keep track of the joystick's position
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
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
}

