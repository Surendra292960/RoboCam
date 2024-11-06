package com.example.robocam.video_stream

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.media.MediaCodec
import android.media.MediaRecorder
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.IntBuffer
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


open class RecordableSurfaceView(context: Context) : SurfaceView(context) {
    private var mSurface: Surface? = null

    private val mRenderMode = AtomicInteger(RENDERMODE_CONTINUOUSLY)

    private var mWidth = 0

    private var mHeight = 0

    private var mDesiredWidth = 0

    private var mDesiredHeight = 0

    private var mPaused = false

    private var mMediaRecorder: MediaRecorder? = null

    private var mARRenderThread: ARRenderThread? = null

    private val mIsRecording = AtomicBoolean(false)

    private val mHasGLContext = AtomicBoolean(false)

    private val mHasGLSurface = AtomicBoolean(false)

    private var mRendererCallbacksWeakReference: WeakReference<RendererCallbacks?>? = null

    private val mSizeChange = AtomicBoolean(false)

    private val mRenderRequested = AtomicBoolean(false)

    /**
     * returns whether or not to attempt to preserve the Context on pause.
     */
    /**
     * Set whether or not EGL Context is preserved when the view is paused & resumed.
     *
     *
     * If true, then EGL Context *may* be preserved.
     *
     *
     *
     * Note that on some devices and older Android versions, the Context may not be
     * preserved due to limitations of GPU hardware.
     *
     *
     *
     * If false, the Context will be released when paused, and recreated when resumed.
     *
     *
     * Default is false.
     * @param preserve preserve the Context on pause
     */
    var preserveEGLContextOnPause: Boolean = false


    /**
     * Performs necessary setup operations such as creating a MediaCodec persistent surface and
     * setting up initial state.
     *
     *
     * Also links the SurfaceHolder that manages the Surface View to the render thread for lifecycle
     * callbacks
     *
     * @see MediaCodec
     *
     * @see SurfaceHolder.Callback
     */
    private fun doSetup() {

        if (!mHasGLSurface.get()) {
            mSurface = MediaCodec.createPersistentInputSurface()
            mARRenderThread = ARRenderThread()
        }

        this.holder.addCallback(mARRenderThread)

        if (holder.surface.isValid) {
            mARRenderThread!!.surfaceCreated(holder)
        }

        mPaused = true
    }


    /**
     * Pauses the render thread.
     */
    fun pause() {
        mPaused = true
    }

    /**
     * Resumes a paused render thread, or in the case of an interrupted or terminated
     * render thread, re-calls [.doSetup] to build/start the GL context again.
     *
     *
     * This method is useful for use in conjunction with the Activity lifecycle
     */
    fun resume() {
        doSetup()
        mPaused = false
    }

    /**
     * Pauses rendering, but is nondestructive at the moment.
     */
    fun stop() {
        mPaused = true
    }


    var renderMode: Int
        /**
         * @return int representing the current render mode of this object
         * @see RecordableSurfaceView.RENDERMODE_WHEN_DIRTY
         *
         * @see RecordableSurfaceView.RENDERMODE_CONTINUOUSLY
         */
        get() = mRenderMode.get()
        /**
         * Set the rendering mode. When renderMode is [RecordableSurfaceView.RENDERMODE_CONTINUOUSLY],
         * the renderer is called repeatedly to re-render the scene. When renderMode is [ ][RecordableSurfaceView.RENDERMODE_WHEN_DIRTY], the renderer only rendered when the surface is
         * created, or when [RecordableSurfaceView.requestRender] is called. Defaults to [ ][RecordableSurfaceView.RENDERMODE_CONTINUOUSLY].
         *
         *
         * Using [RecordableSurfaceView.RENDERMODE_WHEN_DIRTY] can improve battery life and
         * overall system performance by allowing the GPU and CPU to idle when the view does not need
         * to
         * be updated.
         */
        set(mode) {
            mRenderMode.set(mode)
        }

    /**
     * Resets the [MediaRecorder] to let it be cleanly re-initialized without destroying the
     * process
     */
    fun resetMediaRecorder() {
        if (mMediaRecorder == null || mIsRecording.get()) {
            return
        }
        mMediaRecorder!!.reset()
    }


    /**
     * Request that the renderer render a frame.
     * This method is typically used when the render mode has been set to [ ][RecordableSurfaceView.RENDERMODE_WHEN_DIRTY],  so that frames are only rendered on demand.
     * May be called from any thread.
     *
     *
     * Must not be called before a renderer has been set.
     */
    fun requestRender() {
        mRenderRequested.set(true)
    }

    /**
     * Iitializes the [MediaRecorder] ad relies on its lifecycle and requirements.
     *
     * @param saveToFile    the File object to record into. Assumes the calling program has
     * permission to write to this file
     * @param displayWidth  the Width of the display
     * @param displayHeight the Height of the display
     * @param errorListener optional [MediaRecorder.OnErrorListener] for recording state callbacks
     * @param infoListener  optional [MediaRecorder.OnInfoListener] for info callbacks
     * @see MediaRecorder
     */
    @Throws(IOException::class)
    fun initRecorder(
        saveToFile: File, displayWidth: Int, displayHeight: Int,
        errorListener: MediaRecorder.OnErrorListener?, infoListener: MediaRecorder.OnInfoListener?
    ) {
        initRecorder(
            saveToFile, displayWidth, displayHeight, displayWidth, displayHeight, 0,
            errorListener, infoListener
        )
    }


    @Throws(IOException::class)
    fun initRecorder(
        saveToFile: File, displayWidth: Int, displayHeight: Int,
        orientationHint: Int, errorListener: MediaRecorder.OnErrorListener?,
        infoListener: MediaRecorder.OnInfoListener?
    ) {
        initRecorder(
            saveToFile, displayWidth, displayHeight, displayWidth, displayHeight,
            orientationHint, errorListener, infoListener
        )
    }


    /**
     * Iitializes the [MediaRecorder] ad relies on its lifecycle and requirements.
     *
     * @param saveToFile      the File object to record into. Assumes the calling program has
     * permission to write to this file
     * @param displayWidth    the Width of the display
     * @param displayHeight   the Height of the display
     * @param orientationHint the orientation to record the video (0, 90, 180, or 270)
     * @param errorListener   optional [MediaRecorder.OnErrorListener] for recording state callbacks
     * @param infoListener    optional [MediaRecorder.OnInfoListener] for info callbacks
     * @see MediaRecorder
     */
    @Throws(IOException::class)
    fun initRecorder(
        saveToFile: File, displayWidth: Int, displayHeight: Int,
        desiredWidth: Int, desiredHeight: Int, orientationHint: Int,
        errorListener: MediaRecorder.OnErrorListener?,
        infoListener: MediaRecorder.OnInfoListener?
    ) {
        val mediaRecorder = MediaRecorder()

        mediaRecorder.setOnInfoListener(infoListener)

        mediaRecorder.setOnErrorListener(errorListener)

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setInputSurface(mSurface!!)
       // mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
       // mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

       // mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
       // mediaRecorder.setAudioSamplingRate(44100)
       // mediaRecorder.setAudioEncodingBitRate(96000)

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)

        mediaRecorder.setVideoEncodingBitRate(12000000)
        mediaRecorder.setVideoFrameRate(30)

        mDesiredHeight = desiredHeight
        mDesiredWidth = desiredWidth

        mediaRecorder.setVideoSize(mDesiredWidth, mDesiredHeight)

        mediaRecorder.setOrientationHint(orientationHint)

        mediaRecorder.setOutputFile(saveToFile.path)

        mediaRecorder.prepare()

        mMediaRecorder = mediaRecorder
    }


    /**
     * @return true if the recording started successfully and false if not
     * @see MediaRecorder.start
     */
    fun startRecording(): Boolean {
        var success = true
        try {
            mMediaRecorder!!.start()
            //mARRenderThread!!.showDialog(holder)
            mIsRecording.set(true)
        } catch (e: IllegalStateException) {
            success = false
            mIsRecording.set(false)
            mMediaRecorder!!.reset()
            mMediaRecorder!!.release()
        }
        return success
    }

    /**
     * Stops the [MediaRecorder] and sets the internal state of this object to 'Not
     * recording'
     * It is important to call this before attempting to play back the video that has been
     * recorded.
     *
     * @return true if the recording stopped successfully and false if not
     * @throws IllegalStateException if not recording when called
     */
    @Throws(IllegalStateException::class)
    fun stopRecording(): Boolean {
        if (mIsRecording.get()) {
            var success = true
            try {
                mMediaRecorder!!.stop()
                mIsRecording.set(false)
            } catch (e: RuntimeException) {
                success = false
            } finally {
                mMediaRecorder!!.release()
            }
            return success
        } else {
            throw IllegalStateException("Cannot stop. Is not recording.")
        }
    }


    var rendererCallbacks: RendererCallbacks?
        /**
         * Returns the reference (if any) to the [RendererCallbacks]
         *
         * @return the callbacks if registered
         * @see RendererCallbacks
         */
        get() {
            if (mRendererCallbacksWeakReference != null) {
                return mRendererCallbacksWeakReference!!.get()
            }

            return null
        }
        /**
         * Add a [RendererCallbacks] object to handle rendering. Not setting one of these is not
         * necessarily an error, but is usually necessary.
         *
         * @param surfaceRendererCallbacks - the object to call back to
         */
        set(surfaceRendererCallbacks) {
            mRendererCallbacksWeakReference = WeakReference(surfaceRendererCallbacks)
        }


    /**
     * Queue a runnable to be run on the GL rendering thread.
     *
     * @param runnable - the runnable to queue
     */
    fun queueEvent(runnable: Runnable) {
        if (mARRenderThread != null) {
            mARRenderThread!!.mRunnableQueue.add(runnable)
        }
    }

    /**
     * Lifecycle events for the SurfaceView and renderer. These callbacks (unless specified)
     * are executed on the GL thread.
     */
    interface RendererCallbacks {
        /**
         * The surface has been created and bound to the GL context.
         *
         *
         * A GL context is guaranteed to exist when this function is called.
         */
        fun onSurfaceCreated()

        /**
         * The surface has changed width or height.
         *
         *
         * This callback will only be called when there is a change to either or both params
         *
         * @param width  width of the surface
         * @param height height of the surface
         */
        fun onSurfaceChanged(width: Int, height: Int)

        /**
         * Called just before the GL Context is torn down.
         */
        fun onSurfaceDestroyed()


        /**
         * Called when the GL context has been created and has been bound.
         */
        fun onContextCreated()

        /**
         * Called before onDrawFrame, each time as a hook to adjust a global clock for rendering,
         * or other pre-frame modifications that need to be made before rendering.
         */
        fun onPreDrawFrame()

        /**
         * Render call. Called twice when recording: first for screen display, second for video
         * file.
         */
        fun onDrawFrame()
    }


    private inner class ARRenderThread : Thread(), SurfaceHolder.Callback2 {
        var mEGLDisplay: EGLDisplay? = null

        var mEGLContext: EGLContext? = null

        var mEGLSurface: EGLSurface? = null

        var mEGLSurfaceMedia: EGLSurface? = null

        var mRunnableQueue: LinkedList<Runnable> = LinkedList()

        var config: IntArray = intArrayOf(EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, 0x3142, 1, EGL14.EGL_DEPTH_SIZE, 16, EGL14.EGL_NONE)


        private val mLoop = AtomicBoolean(false)

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                config[10] = EGLExt.EGL_RECORDABLE_ANDROID
            }
        }

        fun chooseEglConfig(eglDisplay: EGLDisplay?): EGLConfig? {
            val configsCount = intArrayOf(0)
            val configs = arrayOfNulls<EGLConfig>(1)
            EGL14.eglChooseConfig(eglDisplay, config, 0, configs, 0, configs.size, configsCount, 0)
            return configs[0]
        }

        override fun run() {
            if (mHasGLSurface.get()) {
                return
            }
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)
            val eglConfig = chooseEglConfig(mEGLDisplay)
            if (!mHasGLContext.get()) {
                mEGLContext = EGL14.eglCreateContext(mEGLDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0)
                mHasGLContext.set(true)
            }

            val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)

            mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, eglConfig, this@RecordableSurfaceView, surfaceAttribs, 0)
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)

            // guarantee to only report surface as created once GL context
            // associated with the surface has been created, and call on the GL thread
            // NOT the main thread but BEFORE the codec surface is attached to the GL context
            if (mRendererCallbacksWeakReference != null && mRendererCallbacksWeakReference!!.get() != null) {
                mRendererCallbacksWeakReference!!.get()!!.onSurfaceCreated()
            }

            mEGLSurfaceMedia = EGL14.eglCreateWindowSurface(mEGLDisplay, eglConfig, mSurface, surfaceAttribs, 0)

            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

            mHasGLSurface.set(true)

            if (mRendererCallbacksWeakReference != null && mRendererCallbacksWeakReference!!.get() != null) {
                mRendererCallbacksWeakReference!!.get()!!.onContextCreated()
            }

            mLoop.set(true)

            while (mLoop.get()) {
                if (!mPaused) {
                    var shouldRender = false

                    //we're just rendering when requested, so check that no one
                    //has requested and if not, just continue
                    if (mRenderMode.get() == RENDERMODE_WHEN_DIRTY) {
                        if (mRenderRequested.get()) {
                            mRenderRequested.set(false)
                            shouldRender = true
                        }
                    } else {
                        shouldRender = true
                    }

                    if (mSizeChange.get()) {
                        GLES20.glViewport(0, 0, mWidth, mHeight)
                        Log.d(TAG, "showDialog run : $mWidth $mHeight")

                        if (mRendererCallbacksWeakReference != null && mRendererCallbacksWeakReference!!.get() != null) {
                            mRendererCallbacksWeakReference!!.get()!!.onSurfaceChanged(mWidth, mHeight)
                        }

                        mSizeChange.set(false)
                    }

                    if (shouldRender && mEGLSurface != null && mEGLSurface !== EGL14.EGL_NO_SURFACE) {
                        if (mRendererCallbacksWeakReference != null && mRendererCallbacksWeakReference!!.get() != null) {
                            mRendererCallbacksWeakReference!!.get()!!.onPreDrawFrame()
                        }

                        if (mRendererCallbacksWeakReference != null && mRendererCallbacksWeakReference!!.get() != null) {
                            mRendererCallbacksWeakReference!!.get()!!.onDrawFrame()
                        }

                        EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)

                        if (mIsRecording.get()) {
                            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurfaceMedia, mEGLSurfaceMedia, mEGLContext)
                            if (mRendererCallbacksWeakReference != null && mRendererCallbacksWeakReference!!.get() != null) {
                                GLES20.glViewport(0, 0, mDesiredWidth, mDesiredHeight)
                                mRendererCallbacksWeakReference!!.get()!!.onDrawFrame()
                                GLES20.glViewport(0, 0, mWidth, mHeight)
                            }
                            EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurfaceMedia)
                            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)
                        }
                    }

                    while (mRunnableQueue.size > 0) {
                        val event = mRunnableQueue.remove()
                        event.run()
                    }
                }
                try {
                    sleep((1f / 60f * 1000f).toLong())
                } catch (intex: InterruptedException) {
                    if (mRendererCallbacksWeakReference != null && mRendererCallbacksWeakReference!!.get() != null) {
                        mRendererCallbacksWeakReference!!.get()!!.onSurfaceDestroyed()
                    }

                    if (mEGLDisplay != null) {
                        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, if (preserveEGLContextOnPause) mEGLContext else EGL14.EGL_NO_CONTEXT)

                        if (mEGLSurface != null) {
                            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
                        }

                        if (mEGLSurfaceMedia != null) {
                            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurfaceMedia)
                        }
                        if (!preserveEGLContextOnPause) {
                            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
                            mHasGLContext.set(false)
                        }

                        mHasGLSurface.set(false)
                        EGL14.eglReleaseThread()
                        EGL14.eglTerminate(mEGLDisplay)
                        mSurface!!.release()
                    }
                    return
                }
            }
        }


        override fun surfaceRedrawNeeded(surfaceHolder: SurfaceHolder) {

        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            if ((!this.isAlive && !this.isInterrupted) && this.state != State.TERMINATED) {
                this.start()
            }
        }

        override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, width: Int, height: Int) {
            if (mWidth != width) {
                mWidth = width
                mSizeChange.set(true)
            }

            if (mHeight != height) {
                mHeight = height
                mSizeChange.set(true)
            }
        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            mLoop.set(false)
            this.interrupt()
            holder.removeCallback(this@ARRenderThread)
        }

        fun showDialog(holder: SurfaceHolder?) {
            Log.d(TAG, "showDialog: ")
            showDialog(context = context)
        }
    }

    private fun showDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Dialog Title")
            .setMessage("This is a dialog message.")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    companion object {
        private val TAG: String = RecordableSurfaceView::class.java.simpleName

        /**
         * The renderer only renders when the surface is created, or when @link{requestRender} is
         * called.
         */
        var RENDERMODE_WHEN_DIRTY: Int = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        /**
         * The renderer is called continuously to re-render the scene.
         */
        var RENDERMODE_CONTINUOUSLY: Int = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        private val mEGLContext: EGLContext? = null
    }
}