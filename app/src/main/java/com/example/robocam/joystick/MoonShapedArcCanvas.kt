package com.example.robocam.joystick

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*@Composable
fun MoonShapedArcCanvass() {
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
}*/

@Composable
fun MoonShapedArcCanvas() {
    Box(modifier = Modifier.size(300.dp)
        .drawBehind {
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

        // Draw the arc path with a gradient
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(Color.Cyan, Color.Cyan),
                start = Offset(canvasCenter.x, canvasCenter.y - outerRadius),
                end = Offset(canvasCenter.x, canvasCenter.y + outerRadius)
            ),
            style = Fill
        )

        // Calculate the midpoint of the arc
        val midAngle = -90f + 180f / 2 // Start angle + half sweep angle
        val midAngleRadians = Math.toRadians(midAngle.toDouble())

        // Midpoint coordinates on the arc
        val midX = canvasCenter.x + outerRadius * kotlin.math.cos(midAngleRadians).toFloat()
        val midY = canvasCenter.y + outerRadius * kotlin.math.sin(midAngleRadians).toFloat()

        // Triangle properties
        val triangleHeight = 40f // Height of the triangle
        val triangleBase = 40f // Base width of the triangle
        val rotationAngleDegrees = 90f // Rotation angle for the triangle

        // Rotate a point around the triangle tip
        fun rotatePoint(x: Float, y: Float, centerX: Float, centerY: Float, angleDegrees: Float): Offset {
            val angleRadians = Math.toRadians(angleDegrees.toDouble())
            val cosAngle = kotlin.math.cos(angleRadians).toFloat()
            val sinAngle = kotlin.math.sin(angleRadians).toFloat()

            val dx = x - centerX
            val dy = y - centerY

            val rotatedX = centerX + (dx * cosAngle - dy * sinAngle)
            val rotatedY = centerY + (dx * sinAngle + dy * cosAngle)

            return Offset(rotatedX, rotatedY)
        }

        // Triangle vertices (before rotation)
        val tip = Offset(midX, midY)
        val bottomLeft = Offset(midX - triangleBase / 2, midY + triangleHeight)
        val bottomRight = Offset(midX + triangleBase / 2, midY + triangleHeight)

        // Rotate the triangle vertices
        val rotatedBottomLeft = rotatePoint(bottomLeft.x, bottomLeft.y, tip.x, tip.y, rotationAngleDegrees)
        val rotatedBottomRight = rotatePoint(bottomRight.x, bottomRight.y, tip.x, tip.y, rotationAngleDegrees)

        // Create a triangle path with rotated vertices
        val trianglePath = Path().apply {
            moveTo(tip.x, tip.y) // Tip of the triangle
            lineTo(rotatedBottomLeft.x, rotatedBottomLeft.y) // Bottom left (rotated)
            lineTo(rotatedBottomRight.x, rotatedBottomRight.y) // Bottom right (rotated)
            close()
        }

        // Draw the rotated triangle cap
        drawPath(
            path = trianglePath,
            brush = Brush.linearGradient(
                colors = listOf(Color.Cyan, Color.Cyan),
                start = Offset(canvasCenter.x, canvasCenter.y - outerRadius),
                end = Offset(canvasCenter.x, canvasCenter.y + outerRadius)
            ),
            style = Fill
        )
    }
    )
}


@Composable
@Preview
fun AppContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MoonShapedArcCanvas()
    }
}
