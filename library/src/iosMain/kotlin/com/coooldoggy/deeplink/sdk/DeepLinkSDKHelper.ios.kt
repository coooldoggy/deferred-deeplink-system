package com.coooldoggy.deeplink.sdk

import com.coooldoggy.deeplink.sdk.models.DeepLinkResult
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

/**
 * iOS용 DeepLink SDK 헬퍼
 */
object DeepLinkSDKHelper {
    private const val KEY_DEVICE_ID = "com.deeplink.sdk.device_id"
    private const val KEY_DEEPLINK_CHECKED = "com.deeplink.sdk.deeplink_checked"
    
    private var sdk: DeepLinkSDK? = null
    
    /**
     * SDK 초기화
     * AppDelegate.didFinishLaunchingWithOptions에서 호출
     */
    fun initialize(serverUrl: String) {
        sdk = DeepLinkSDK(
            baseUrl = serverUrl,
            deviceInfoProvider = DeviceInfoProvider()
        )
    }
    
    /**
     * Deferred Deep Link 확인
     * ViewController.viewDidLoad()에서 호출
     * 
     * @param callback 결과 콜백
     */
    fun checkDeferredDeepLink(callback: (DeepLinkResult) -> Unit) {
        val currentSdk = sdk ?: throw IllegalStateException(
            "SDK not initialized. Call initialize() first."
        )
        
        val userDefaults = NSUserDefaults.standardUserDefaults
        
        // 이미 확인했는지 체크
        if (userDefaults.boolForKey(KEY_DEEPLINK_CHECKED)) {
            callback(DeepLinkResult.NoMatch)
            return
        }
        
        // 디바이스 ID 가져오기 또는 생성
        val deviceId = getOrCreateDeviceId(userDefaults)
        
        // 매칭 수행
        currentSdk.checkDeferredDeepLink(deviceId) { result ->
            // 확인 완료 표시
            userDefaults.setBool(true, KEY_DEEPLINK_CHECKED)
            callback(result)
        }
    }
    
    /**
     * SDK suspend 함수 사용
     */
    suspend fun checkDeferredDeepLinkSuspend(): DeepLinkResult {
        val currentSdk = sdk ?: throw IllegalStateException(
            "SDK not initialized. Call initialize() first."
        )
        
        val userDefaults = NSUserDefaults.standardUserDefaults
        
        // 이미 확인했는지 체크
        if (userDefaults.boolForKey(KEY_DEEPLINK_CHECKED)) {
            return DeepLinkResult.NoMatch
        }
        
        // 디바이스 ID 가져오기 또는 생성
        val deviceId = getOrCreateDeviceId(userDefaults)
        
        // 매칭 수행
        val result = currentSdk.checkDeferredDeepLinkSuspend(deviceId)
        
        // 확인 완료 표시
        userDefaults.setBool(true, KEY_DEEPLINK_CHECKED)
        
        return result
    }
    
    /**
     * Deferred Deep Link 확인 상태 리셋 (테스트용)
     */
    fun resetDeferredDeepLinkCheck() {
        val userDefaults = NSUserDefaults.standardUserDefaults
        userDefaults.setBool(false, KEY_DEEPLINK_CHECKED)
    }
    
    private fun getOrCreateDeviceId(userDefaults: NSUserDefaults): String {
        var deviceId = userDefaults.stringForKey(KEY_DEVICE_ID)
        if (deviceId == null) {
            deviceId = NSUUID().UUIDString()
            userDefaults.setObject(deviceId, KEY_DEVICE_ID)
        }
        return deviceId
    }
}

