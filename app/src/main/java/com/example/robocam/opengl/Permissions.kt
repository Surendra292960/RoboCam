package com.example.robocam.opengl

import android.Manifest
import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions(private val activity: Activity, private val permissionList: List<String>, val code: Int) {

    fun checkPermissions() {
        if (permissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        }
    }

    private fun permissionsGranted(): Int {
        var counter = 0
        for (permission in permissionList) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }

    private fun deniedPermission(): String {
        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }

    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

        } else {

            ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), code)
        }
    }

    fun checkPermission(manifest: String): Boolean {
        val result = ContextCompat.checkSelfPermission(activity, manifest)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionActivity(state: Boolean, urlPath: String, kind : String){
        val showRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) && this.activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            true
        }

        if (!showRationale) {

            return
        } else {

            return
        }
    }

    fun getAllPermissions(): List<String> {
        val granted = ArrayList<String>()
        val pi = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_PERMISSIONS)
        for (i in pi.requestedPermissions.indices) {
            if (pi.requestedPermissionsFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                granted.add(pi.requestedPermissions[i])
            }
        }

        return granted
    }


}
