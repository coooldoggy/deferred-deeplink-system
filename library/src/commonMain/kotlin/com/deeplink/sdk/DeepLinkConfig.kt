package com.deeplink.sdk

/**
 * DeepLink SDK 설정
 */
data class DeepLinkConfig(
    val serverUrl: String,
    val enableLogging: Boolean = false
)

/**
 * DeepLink SDK 빌더
 */
class DeepLinkSDKBuilder {
    private var serverUrl: String = "http://localhost:8080"
    private var enableLogging: Boolean = false
    
    fun serverUrl(url: String) = apply {
        this.serverUrl = url
    }
    
    fun enableLogging(enable: Boolean) = apply {
        this.enableLogging = enable
    }
    
    fun build(): DeepLinkSDK {
        return DeepLinkSDK(
            baseUrl = serverUrl,
            deviceInfoProvider = DeviceInfoProvider()
        )
    }
}

