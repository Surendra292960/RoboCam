package com.example.robocam.joystick

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MoonShapedArcCanvas() {
    Canvas(modifier = Modifier.size(300.dp)) {
        val canvasCenter = Offset(size.width / 2, size.height / 2) // Calculate center of the canvas
        val outerRadius = size.minDimension / 2 // Outer arc radius
        val innerRadius = outerRadius - 50f // Inner arc radius, controls thickness

        // Define the arc path
        val path = Path().apply {
            // Outer arc
            arcTo(
                rect = Rect(
                    left = canvasCenter.x - innerRadius + 20,
                    top = canvasCenter.y - innerRadius - 20,
                    right = canvasCenter.x + innerRadius + 20,
                    bottom = canvasCenter.y + innerRadius + 40
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )

            // Inner arc (reverse direction for closing the path)
            arcTo(
                rect = Rect(
                    left = canvasCenter.x - innerRadius + 40,
                    top = canvasCenter.y - innerRadius - 20,
                    right = canvasCenter.x + innerRadius - 20,
                    bottom = canvasCenter.y + innerRadius + 40
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )

            close() // Close the path
        }

        // Draw the path with a gradient
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(Color.Cyan, Color.Transparent),
                start = Offset(canvasCenter.x, canvasCenter.y - outerRadius),
                end = Offset(canvasCenter.x, canvasCenter.y + outerRadius)
            ),
            style = Fill
        )
    }
}

@Composable
@Preview
fun AppContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MoonShapedArcCanvas()
    }
}
