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
            Bitmap bitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(0); // Clear background

            // Draw background
            Drawable background = context.getResources().getDrawable(R.drawable.square);
            background.setBounds(0, 0, 1024, 1024);
            background.draw(canvas);

            // Paint for title
            Paint titlePaint = new Paint();
            titlePaint.setTextSize(80); // High-resolution text size
            titlePaint.setAntiAlias(true);
            titlePaint.setDither(true);
            titlePaint.setARGB(255, 0, 0, 0); // Black text

            // Paint for description
            Paint descriptionPaint = new Paint();
            descriptionPaint.setTextSize(40); // Smaller but high-resolution text
            descriptionPaint.setAntiAlias(true);
            descriptionPaint.setDither(true);
            descriptionPaint.setARGB(255, 0, 0, 0);

            // Draw the title
            String[] titleLines = wrapText(titlePaint, title, 900); // Fit within 900px width
            float y = 100; // Starting y-coordinate for title
            for (String line : titleLines) {
                canvas.drawText(line, 50, y, titlePaint); // Left margin of 50px
                y += titlePaint.getTextSize() + 20; // Add line spacing
            }

            // Draw the description
            String[] descriptionLines = wrapText(descriptionPaint, description, 900);
            y += 40; // Add spacing between title and description
            for (String line : descriptionLines) {
                if (y + descriptionPaint.getTextSize() > bitmap.getHeight()) {
                    break; // Avoid drawing out of bounds
                }
                canvas.drawText(line, 50, y, descriptionPaint);
                y += descriptionPaint.getTextSize() + 10; // Add line spacing
            }

            // Bind texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR); // Smooth scaling
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle(); // Free memory
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