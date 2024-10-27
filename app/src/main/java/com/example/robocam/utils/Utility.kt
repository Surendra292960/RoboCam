package com.example.robocam.utils

import android.opengl.GLES20
import android.util.Log

object Utility {

    fun checkGLError() {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("OpenGL Error", "Error: $error")
        }
    }

    fun glLinkProgram(program: Int) {
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == GLES20.GL_FALSE) {
            val logLength = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_INFO_LOG_LENGTH, logLength, 0)
            val log = ByteArray(logLength[0])
            GLES20.glGetProgramInfoLog(program)
            Log.e("Program Error", "Error linking program: ${String(log)}")
            GLES20.glDeleteProgram(program)
        }
    }

    fun checkFramebufferStatus() {
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        when (status) {
            GLES20.GL_FRAMEBUFFER_COMPLETE -> {
                Log.d("Framebuffer Status", "Framebuffer is complete")
            }
            GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> {
                Log.e("Framebuffer Error", "Framebuffer incomplete: Incomplete attachment")
            }
            GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> {
                Log.e("Framebuffer Error", "Framebuffer incomplete: Missing attachment")
            }
            GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS -> {
                Log.e("Framebuffer Error", "Framebuffer incomplete: Incomplete dimensions")
            }
            GLES20.GL_FRAMEBUFFER_UNSUPPORTED -> {
                Log.e("Framebuffer Error", "Framebuffer unsupported")
            }
            else -> {
                Log.e("Framebuffer Error", "Unknown framebuffer error: $status")
            }
        }
    }

    fun compileShader(type: Int, source: String): Int {
        // Load shader
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        println("Shader : $shader status : ${compileStatus[0]}")
        if (compileStatus[0] == 0) {
            val logLength = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_INFO_LOG_LENGTH, logLength, 0)
            val log = ByteArray(logLength[0])
            GLES20.glGetShaderInfoLog(shader)
            println("Shader compilation error: ${String(log)}")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     *
     * **Note:** When developing shaders, use the checkGlError()
     * method to debug shader coding errors.
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    fun loadShader(type: Int, shaderCode: String?): Int {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)

        val shader = GLES20.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        return shader
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    fun checkGlError(glOperation: String) {
        var error: Int
        while ((GLES20.glGetError().also { error = it }) != GLES20.GL_NO_ERROR) {
            Log.e("TAG", "$glOperation: glError $error")
            throw RuntimeException("$glOperation: glError $error")
        }
    }
}