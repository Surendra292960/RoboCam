import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DraggableDPad(modifier: Modifier = Modifier, size: Dp = 200.dp,
                  //onInput: (velocity: Float, angle: Float) -> Unit
                  onInput: (velocityX: Float, velocityY: Float) -> Unit
) {
    // State to track touch position
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    //val center = with(LocalDensity.current) { size.toPx() / 2 }
    val radius = with(LocalDensity.current) { size.toPx() / 2 }

    Box(
        modifier = Modifier
            .size(size)
            .background(Color.Gray, shape = CircleShape) // DPad appearance
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                       /* touchPosition = change.position
                        handleTouch(touchPosition, center, onInput)*/

                        val touchPosition = change.position
                        val center = Offset(radius, radius)
                        val (velocityX, velocityY) = calculateVelocity(touchPosition, center, radius)
                        onInput(velocityX, velocityY)
                    },
                    onDragEnd = {
                        onInput(0f, 0f) // Reset velocity on release
                        //touchPosition = Offset.Zero
                    }
                )
            }
    ) {
        // Optional: Draw the center or feedback
   /*     Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.DarkGray, radius = center / 10)
        }*/
    }
}



fun handleTouch(touch: Offset, center: Float, onInput: (velocity: Float, angle: Float) -> Unit) {
    val dx = touch.x - center
    val dy = touch.y - center

    // Calculate distance and normalize it
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
    val maxDistance = center // Radius of the DPad
    val velocity = (distance / maxDistance).coerceIn(0f, 1f)

    // Calculate angle (in radians) and convert to degrees
    val angle = kotlin.math.atan2(dy, dx) * (180 / Math.PI).toFloat()
    val normalizedAngle = if (angle < 0) angle + 360 else angle

    // Pass results to the onInput callback
    onInput(velocity, normalizedAngle)
}

fun calculateVelocity(touch: Offset, center: Offset, radius: Float): Pair<Float, Float> {
    // Displacement from center
    val dx = touch.x - center.x
    val dy = touch.y - center.y

    // Distance from center
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)

    // Normalize velocity scalar
    val velocityScalar = (distance / radius).coerceIn(0f, 1f)

    // Avoid division by zero
    if (distance == 0f) return Pair(0f, 0f)

    // Unit vector components
    val unitX = dx / distance
    val unitY = dy / distance

    // Velocity components
    val velocityX = velocityScalar * unitX
    val velocityY = velocityScalar * unitY

    return Pair(velocityX, velocityY)
}
