package com.example.robocam.video_stream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView implements Runnable {
    private Thread thread;
    private boolean isRunning;
    private SurfaceHolder surfaceHolder;
    private Paint paint;

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
    }

    @Override
    public void run() {
        while (isRunning) {
            draw();
        }
    }

    public void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE); // Clear the canvas
            canvas.drawText("Hello, SurfaceView!", 100, 100, paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
}