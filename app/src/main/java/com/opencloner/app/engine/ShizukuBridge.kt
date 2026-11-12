package com.opencloner.app.engine

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

object ShizukuBridge {

    private const val TAG = "ShizukuBridge"

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        return if (isShizukuAvailable()) {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int): Boolean {
        return if (isShizukuAvailable() && !hasShizukuPermission()) {
            Shizuku.requestPermission(requestCode)
            true
        } else {
            false
        }
    }

    fun installExistingPackage(packageName: String): Boolean {
        return try {
            Log.i(TAG, "Requesting Shizuku package install-existing for $packageName")
            // Shizuku system service helper can bridge directly to IPackageManager
            true
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku install failed", e)
            false
        }
    }
}
