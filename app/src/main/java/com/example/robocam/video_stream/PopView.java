package com.example.robocam.video_stream;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PopView extends SurfaceView implements Runnable {
    Paint paint;
    int screenheight;
    int screenwidth;
    float density;

    volatile int cx;
    volatile int cy;
    int radius;
    volatile int opx = 1;
    volatile int opy = 1;

    volatile boolean running = false;
    SurfaceHolder holder;
    Thread renderThread = null;

    public PopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        screenheight = dm.heightPixels;
        screenwidth = dm.widthPixels;
        density = dm.density;
        radius = Math.round(20 * density);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);
    }

    public void resume() {
        running = true;
        renderThread = new Thread(this);
        renderThread.start();
        opx = 1;
        opy = 1;
        cx = 0;
        cy = 0;
    }

    @Override
    public void run() {
        while (running) {
            if (!holder.getSurface().isValid()) continue; //wait till it becomes valid
            Canvas canvas = holder.lockCanvas();
            canvas.drawARGB(0xff, 0, 0, 0xff);
            canvas.drawCircle(cx, cy, radius, paint);
            holder.unlockCanvasAndPost(canvas);
            update();
        }
    }

    private void update() {
        cx += opx;
        cy += opy;
        if (cy > screenheight) {
            opy = -1;
        } else if (cy < 0) {
            opy = +1;
        }

        if (cx > screenwidth) {
            opx = -1;
        } else if (cx < 0) {
            opx = +1;
        }
    }

    public void pause() {
        running = false;
        boolean retry = true;
        while (retry) {
            try {
                renderThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //retry
            }
        }
    }
}