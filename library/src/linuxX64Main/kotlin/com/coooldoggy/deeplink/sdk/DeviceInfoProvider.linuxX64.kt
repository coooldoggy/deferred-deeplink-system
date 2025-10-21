package com.coooldoggy.deeplink.sdk

actual class DeviceInfoProvider {
    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            userAgent = "DeepLinkSDK/1.0 (Linux; Native)",
            deviceModel = "Linux x64",
            osName = "Linux",
            osVersion = "Unknown",
            language = "en",
            timezone = "UTC",
            screenResolution = null
        )
    }
}

