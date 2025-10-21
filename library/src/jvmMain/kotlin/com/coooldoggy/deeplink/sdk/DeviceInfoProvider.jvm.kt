package com.coooldoggy.deeplink.sdk

actual class DeviceInfoProvider {
    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            userAgent = buildUserAgent(),
            deviceModel = getDeviceModel(),
            osName = getOsName(),
            osVersion = getOsVersion(),
            language = getLanguage(),
            timezone = getTimezone(),
            screenResolution = null
        )
    }
    
    private fun buildUserAgent(): String {
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        return "DeepLinkSDK/1.0 ($osName $osVersion; JVM)"
    }
    
    private fun getDeviceModel(): String {
        return System.getProperty("os.arch") ?: "Unknown"
    }
    
    private fun getOsName(): String {
        return System.getProperty("os.name") ?: "Unknown"
    }
    
    private fun getOsVersion(): String {
        return System.getProperty("os.version") ?: "Unknown"
    }
    
    private fun getLanguage(): String {
        return System.getProperty("user.language") ?: "en"
    }
    
    private fun getTimezone(): String {
        return java.util.TimeZone.getDefault().id
    }
}

