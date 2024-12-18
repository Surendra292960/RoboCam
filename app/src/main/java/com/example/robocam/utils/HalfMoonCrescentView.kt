package com.example.robocam.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class HalfMoonCrescentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF6F00.toInt() // Orange color for the crescent
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Outer Circle (Full Moon)
        val outerPath = Path().apply {
            addCircle(width / 2, height / 2, width / 2, Path.Direction.CW)
        }

        // Inner Circle (Cutout to form the crescent)
        val innerPath = Path().apply {
            addCircle(width / 1.8f, height / 2, width / 2.5f, Path.Direction.CCW)
        }

        // Combine Paths (Outer - Inner = Crescent)
        outerPath.op(innerPath, Path.Op.DIFFERENCE)

        // Clip the Crescent to show only half (left or right side)
        val clipPath = Path().apply {
            addRect(width / 2, 0f, width, height, Path.Direction.CW) // Right half
        }
        outerPath.op(clipPath, Path.Op.DIFFERENCE)

        // Draw the Half Moon Crescent
        canvas.drawPath(outerPath, paint)
    }
}

