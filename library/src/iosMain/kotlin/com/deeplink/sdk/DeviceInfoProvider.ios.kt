package com.deeplink.sdk

import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class DeviceInfoProvider {
    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            userAgent = buildUserAgent(),
            deviceModel = getDeviceModel(),
            osName = "iOS",
            osVersion = UIDevice.currentDevice.systemVersion,
            language = getLanguage(),
            timezone = getTimezone(),
            screenResolution = getScreenResolution()
        )
    }
    
    private fun buildUserAgent(): String {
        val device = UIDevice.currentDevice
        val systemVersion = device.systemVersion
        val model = getDeviceModel()
        return "DeepLinkSDK/1.0 (iOS $systemVersion; $model)"
    }
    
    private fun getDeviceModel(): String {
        // UIDevice.currentDevice.model은 "iPhone", "iPad" 같은 일반적인 이름만 반환
        // 더 구체적인 모델명이 필요한 경우 sysctl을 사용할 수 있음
        return UIDevice.currentDevice.model
    }
    
    private fun getLanguage(): String {
        val preferredLanguages = NSLocale.preferredLanguages
        return if (preferredLanguages.isNotEmpty()) {
            preferredLanguages[0] as String
        } else {
            "en"
        }
    }
    
    private fun getTimezone(): String {
        return NSTimeZone.localTimeZone.name
    }
    
    private fun getScreenResolution(): String {
        val screen = UIScreen.mainScreen
        val scale = screen.scale
        val nativeBounds = screen.nativeBounds
        
        val width = nativeBounds.size.width.toInt()
        val height = nativeBounds.size.height.toInt()
        
        return "${width}x${height}"
    }
}

