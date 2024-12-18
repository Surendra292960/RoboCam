package com.example.robocam.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class CrescentView @JvmOverloads constructor(
    context: Context, 
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF6F00.toInt() // Orange color
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Outer circle
        val outerPath = Path().apply {
            addCircle(width / 2, height / 2, width / 2, Path.Direction.CW)
        }

        // Inner circle (cut-out)
        val innerPath = Path().apply {
            addCircle(width / 2.5f, height / 2, width / 3, Path.Direction.CCW)
        }

        // Combine the paths
        outerPath.op(innerPath, Path.Op.DIFFERENCE)

        // Draw the crescent
        canvas.drawPath(outerPath, paint)
    }
}
