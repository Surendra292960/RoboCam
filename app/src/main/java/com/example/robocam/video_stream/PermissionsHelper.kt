package com.example.robocam.video_stream

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build

object PermissionsHelper {
    private const val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE

    private const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO


    private const val GRANT_REQUEST_CODE = 1


    fun hasPermissions(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.checkSelfPermission(RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED
        } else {
            activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                    (activity.checkSelfPermission(RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED)
        }
    }

    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    fun requestPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.requestPermissions(arrayOf(RECORD_AUDIO_PERMISSION), GRANT_REQUEST_CODE)
        } else {
            activity.requestPermissions(
                arrayOf(WRITE_EXTERNAL_STORAGE_PERMISSION, RECORD_AUDIO_PERMISSION),
                GRANT_REQUEST_CODE
            )
        }
    }
}
