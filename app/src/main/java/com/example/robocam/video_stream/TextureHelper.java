package com.example.robocam.video_stream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.robocam.R;

import java.util.ArrayList;

public class TextureHelper {
    public static int loadTexture(final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;    // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
    public static int loadText(final Context context, String title, String description) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            // Create a high-resolution bitmap
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(0);

            // Draw background
            Drawable background = context.getResources().getDrawable(R.drawable.square);
            background.setBounds(0, 0, 512, 512);
            background.draw(canvas);

            // Title Paint
            Paint titlePaint = new Paint();
            titlePaint.setTextSize(40); // Larger title size
            titlePaint.setAntiAlias(true);
            titlePaint.setDither(true);
            titlePaint.setARGB(0xff, 0x00, 0x00, 0x00);

            // Description Paint
            Paint descriptionPaint = new Paint();
            descriptionPaint.setTextSize(24); // Larger text size for better visibility
            descriptionPaint.setAntiAlias(true);
            descriptionPaint.setDither(true);
            descriptionPaint.setARGB(0xff, 0x00, 0x00, 0x00);

            // Draw the title
            String[] titleLines = wrapText(titlePaint, title, 400); // Fit within 400px width
            float y = 60;
            for (String line : titleLines) {
                canvas.drawText(line, 20, y, titlePaint);
                y += titlePaint.getTextSize() + 10; // Line spacing
            }

            // Draw the description
            String[] descriptionLines = wrapText(descriptionPaint, description, 400);
            y += 20; // Padding between title and description
            for (String line : descriptionLines) {
                canvas.drawText(line, 20, y, descriptionPaint);
                y += descriptionPaint.getTextSize() + 8; // Line spacing
            }

            // Bind texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    // Helper method for wrapping text
    private static String[] wrapText(Paint paint, String text, int maxWidth) {
        String[] words = text.split(" ");
        StringBuilder wrappedLine = new StringBuilder();
        ArrayList<String> lines = new ArrayList<>();

        for (String word : words) {
            if (paint.measureText(wrappedLine + word) <= maxWidth) {
                wrappedLine.append(word).append(" ");
            } else {
                lines.add(wrappedLine.toString().trim());
                wrappedLine = new StringBuilder(word).append(" ");
            }
        }
        lines.add(wrappedLine.toString().trim());
        return lines.toArray(new String[0]);
    }

}