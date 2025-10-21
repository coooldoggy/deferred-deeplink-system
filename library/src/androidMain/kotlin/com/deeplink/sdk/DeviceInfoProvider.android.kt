package com.deeplink.sdk

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.Locale
import java.util.TimeZone

actual class DeviceInfoProvider {
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    
    actual fun getDeviceInfo(): DeviceInfo {
        val ctx = context ?: throw IllegalStateException(
            "DeviceInfoProvider not initialized. Call init(context) first."
        )
        
        return DeviceInfo(
            userAgent = buildUserAgent(),
            deviceModel = getDeviceModel(),
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            language = getLanguage(),
            timezone = getTimezone(),
            screenResolution = getScreenResolution(ctx)
        )
    }
    
    private fun buildUserAgent(): String {
        return "DeepLinkSDK/1.0 (Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL})"
    }
    
    private fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else {
            "${manufacturer.capitalize()} $model"
        }
    }
    
    private fun getLanguage(): String {
        return Locale.getDefault().toString()
    }
    
    private fun getTimezone(): String {
        return TimeZone.getDefault().id
    }
    
    private fun getScreenResolution(context: Context): String {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}

