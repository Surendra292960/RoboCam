package com.example.robocam.joystick

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val joystickPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }

    private val stickPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    private var centerPoint = PointF()
    private var stickPoint = PointF()
    private var radius = 0f

    private var movementHandler: Handler = Handler()
    private var isMoving = false
    private var movementRunnable: Runnable? = null

    init {
        setBackgroundColor(Color.BLACK)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerPoint.set(w / 2f, h / 2f)
        radius = Math.min(w, h) / 3f
        stickPoint.set(centerPoint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerPoint.x, centerPoint.y, radius, joystickPaint)
        canvas.drawCircle(stickPoint.x, stickPoint.y, radius / 3, stickPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_DOWN -> {
                val dx = event.x - centerPoint.x
                val dy = event.y - centerPoint.y
                val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (distance < radius) {
                    stickPoint.set(event.x, event.y)
                } else {
                    val angle = atan2(dy.toDouble(), dx.toDouble())
                    stickPoint.x = centerPoint.x + (cos(angle) * radius).toFloat()
                    stickPoint.y = centerPoint.y + (sin(angle) * radius).toFloat()
                }

                checkContinuousMovement()

                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                stickPoint.set(centerPoint)
                stopContinuousMovement()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun checkContinuousMovement() {
        val dx = stickPoint.x - centerPoint.x
        val dy = stickPoint.y - centerPoint.y
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        Log.d("TAG", "JoyStick radius: $distance, $radius")
        if (distance >= radius * 0.9) { // Near edge
            if (!isMoving) {
                startContinuousMovement(dx, dy)
            }
        } else {
            stopContinuousMovement()
        }
    }

    private fun startContinuousMovement(dx: Float, dy: Float) {
        isMoving = true
        movementRunnable = object : Runnable {
            override fun run() {
                // Perform movement logic here
                // Example: Log.d("Joystick", "Moving: dx: $dx, dy: $dy")

                // Log.d("TAG", "startContinuousMovement: Moving: dx: $dx, dy: $dy")
                getStickPosition().also {
                    Log.d("TAG", "startContinuousMovement:Joystick ${it.first} ${it.second}")
                }

                movementHandler.postDelayed(this, 100) // Adjust the delay as needed
            }
        }
        movementHandler.post(movementRunnable!!)
    }

    private fun stopContinuousMovement() {
        isMoving = false
        if (movementRunnable!=null){
            movementHandler.removeCallbacks(movementRunnable!!)
        }
        Log.d("TAG", "stopContinuousMovement: ")
    }

    fun getStickPosition(): Pair<Float, Float> {
        val normalizedX = (stickPoint.x - centerPoint.x) / radius
        val normalizedY = (stickPoint.y - centerPoint.y) / radius
        return Pair(normalizedX, normalizedY) // Between -1 and 1
    }
}