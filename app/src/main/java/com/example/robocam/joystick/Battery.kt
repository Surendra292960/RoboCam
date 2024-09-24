package com.example.robocam.joystick

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.robocam.ui.theme.PsGreen
import kotlin.math.roundToInt

@Composable
fun Battery(
    modifier: Modifier = Modifier,
    value:Int,
    steps:Int = 10,
    outerThickness:Float = 30f,
    totalBarSpace:Float = 120f,
    color: Color,
    knobLength:Float = 45f
) {

    Canvas(
        modifier = modifier
    ){
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(
            color = color,
            size = Size(
                width = canvasWidth,
                height = canvasHeight
            ),
            style = Stroke(
                width = outerThickness,
                pathEffect = PathEffect.cornerPathEffect(1.dp.toPx())
            )
        )
        drawRoundRect(
            color = PsGreen,
            topLeft = Offset(canvasWidth,canvasHeight*0.25f),
            size = Size(knobLength,canvasHeight*0.5f),
            cornerRadius = CornerRadius(1f,1f)
        )
        val innerBatteryWidth = canvasWidth - outerThickness
        val spaceBetween = totalBarSpace / (steps+1)
        val loadingBarWidth = (innerBatteryWidth-totalBarSpace)/steps

        var currentStartOffset = Offset(
            x = (outerThickness/2f) + (loadingBarWidth/2f)+spaceBetween,
            y = outerThickness
        )

        var currentEndOffset = Offset(
            x = (outerThickness/2f) + (loadingBarWidth/2f)+spaceBetween,
            y = canvasHeight - outerThickness
        )

        for(i in 0 until (value/100f*steps).roundToInt()){
            drawLine(
                color = color,
                strokeWidth = loadingBarWidth,
                start = currentStartOffset,
                end = currentEndOffset
            )
            currentStartOffset =
                currentStartOffset.copy(x = currentStartOffset.x + loadingBarWidth+spaceBetween)
            currentEndOffset =
                currentEndOffset.copy(x = currentEndOffset.x + loadingBarWidth+spaceBetween)
        }
    }
}