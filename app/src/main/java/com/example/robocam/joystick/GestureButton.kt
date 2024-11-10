package com.example.robocam.joystick

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun GestureButton() {
    var coordinates by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Button(
        onClick = {},
        modifier = Modifier
            .background(Color.Gray, shape = RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    // Capture the current touch position
                    coordinates = coordinates + change.position
                }
            }
    ) {
        Text("Draw Here")
    }

    // Canvas to display the captured coordinates
    Canvas(modifier = Modifier
        .background(Color.Transparent)
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                // Capture the current touch position
                coordinates = coordinates + change.position
            }
        }
    ) {
        for (coordinate in coordinates) {
            drawCircle(color = Color.Red, radius = 5f, center = coordinate)
        }
    }
}