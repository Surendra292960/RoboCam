/*
package com.example.robocam

import android.content.Context
import android.hardware.Camera
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyGLRenderer

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY

        val camera: Camera = Camera.open()
        camera.setPreviewDisplay(holder)
        camera.startPreview()

    }


}
*/
