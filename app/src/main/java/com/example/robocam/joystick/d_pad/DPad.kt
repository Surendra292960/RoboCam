package com.example.robocam.joystick.d_pad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DPad(
    onDirectionPressed: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.Gray, shape = CircleShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Up Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(50.dp)
                .clickable { onDirectionPressed("UP") }
                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("↑", color = Color.White)
        }

        // Down Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(50.dp)
                .clickable { onDirectionPressed("DOWN") }
                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("↓", color = Color.White)
        }

        // Left Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(50.dp)
                .clickable { onDirectionPressed("LEFT") }
                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = Color.White)
        }

        // Right Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(50.dp)
                .clickable { onDirectionPressed("RIGHT") }
                .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("→", color = Color.White)
        }
    }
}
