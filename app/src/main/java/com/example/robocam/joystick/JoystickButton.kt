/*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
@Composable
fun JoystickButton(
    modifier: Modifier = Modifier,
    onPositionChanged: (Float, Float) -> Unit // Callback for position changes
) {
    var (x, y) = remember { mutableStateOf(Pair(0f, 0f)) }

    Box(modifier = modifier) {
        // Draw the base circle
        CircleShape(color = Color.Gray)

        // Draw the knob (movable circle)
        var x by remember { mutableStateOf(0f) }
        var y by remember { mutableStateOf(0f) }

        Box(
            modifier = modifier.
                drawBehind {
                    drawCircle(
                        color = Color.Red,
                        radius = size.minDimension / 2f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }
                .offset { IntOffset(x.toInt(), y.toInt()) }
                .draggable(
                    onDragStarted = { x = it.x; y = it.y },
                    onDragStopped = {}
                  */
/*  onDrag = { delta ->
                        x += delta.x; y += delta.y
                        onPositionChanged(x, y)
                    }*//*

                )
        )
    }
}

@Composable
fun CircleShape(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            radius = size.minDimension / 2f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}*/
