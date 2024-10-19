package com.example.robocam

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class ComplexShapes : Drawable() {
    private val paint: Paint = Paint().apply {
        // Create your paint here
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.RED
    }

    override fun draw(canvas: Canvas) {
        val path = Path().apply {
            // Draws triangle
            moveTo(100f, 100f)
            lineTo(300f, 300f)
            lineTo(100f, 300f)
            close()

            // Draws bridge
            moveTo(100f, 400f)
            lineTo(600f, 400f)
            lineTo(600f, 700f)
            lineTo(500f, 700f)
            // bottom is 900 because the system draws the arc inside an
            // imaginary rectangle
            arcTo(200f, 500f, 500f, 900f, 0f, -180f, false)
            lineTo(100f, 700f)
            close()

            // Draws quarter moon
            moveTo(100f, 800f)
            addArc(100f, 800f, 600f, 1_300f, 90f, -180f)
            quadTo(450f, 1_050f, 350f, 1_300f)
            close()
        }

        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        // Required but can be left empty
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // Required but can be left empty
    }

    @Deprecated("Deprecated by super class")
    override fun getOpacity() = PixelFormat.OPAQUE
}

