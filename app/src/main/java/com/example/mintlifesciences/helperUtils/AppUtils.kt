package com.example.mintlifesciences.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log

object AppUtils {

    // Function to get the app version
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("AppUtils", "Package name not found", e)
            "1.0.0" // Default version
        }
    }
}
