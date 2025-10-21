package com.deeplink.sdk

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Android용 DeepLink SDK 헬퍼
 */
object DeepLinkSDKHelper {
    private const val PREFS_NAME = "deeplink_sdk_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_DEEPLINK_CHECKED = "deeplink_checked"
    
    private var sdk: DeepLinkSDK? = null
    private var deviceInfoProvider: DeviceInfoProvider? = null
    
    /**
     * SDK 초기화
     * Application.onCreate()에서 호출
     */
    fun initialize(application: Application, serverUrl: String) {
        // DeviceInfoProvider 초기화
        deviceInfoProvider = DeviceInfoProvider().apply {
            init(application)
        }
        
        // SDK 생성
        sdk = DeepLinkSDK(
            baseUrl = serverUrl,
            deviceInfoProvider = deviceInfoProvider!!
        )
    }
    
    /**
     * Deferred Deep Link 확인
     * MainActivity.onCreate()에서 호출
     * 
     * @param context Context
     * @param callback 결과 콜백
     */
    fun checkDeferredDeepLink(
        context: Context,
        callback: (com.deeplink.sdk.models.DeepLinkResult) -> Unit
    ) {
        val currentSdk = sdk ?: throw IllegalStateException(
            "SDK not initialized. Call initialize() first."
        )
        
        val prefs = getPrefs(context)
        
        // 이미 확인했는지 체크
        if (prefs.getBoolean(KEY_DEEPLINK_CHECKED, false)) {
            callback(com.deeplink.sdk.models.DeepLinkResult.NoMatch)
            return
        }
        
        // 디바이스 ID 가져오기 또는 생성
        val deviceId = getOrCreateDeviceId(prefs)
        
        // 매칭 수행
        currentSdk.checkDeferredDeepLink(deviceId) { result ->
            // 확인 완료 표시
            prefs.edit().putBoolean(KEY_DEEPLINK_CHECKED, true).apply()
            callback(result)
        }
    }
    
    /**
     * SDK suspend 함수 사용
     */
    suspend fun checkDeferredDeepLinkSuspend(context: Context): com.deeplink.sdk.models.DeepLinkResult {
        val currentSdk = sdk ?: throw IllegalStateException(
            "SDK not initialized. Call initialize() first."
        )
        
        val prefs = getPrefs(context)
        
        // 이미 확인했는지 체크
        if (prefs.getBoolean(KEY_DEEPLINK_CHECKED, false)) {
            return com.deeplink.sdk.models.DeepLinkResult.NoMatch
        }
        
        // 디바이스 ID 가져오기 또는 생성
        val deviceId = getOrCreateDeviceId(prefs)
        
        // 매칭 수행
        val result = currentSdk.checkDeferredDeepLinkSuspend(deviceId)
        
        // 확인 완료 표시
        prefs.edit().putBoolean(KEY_DEEPLINK_CHECKED, true).apply()
        
        return result
    }
    
    /**
     * Deferred Deep Link 확인 상태 리셋 (테스트용)
     */
    fun resetDeferredDeepLinkCheck(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_DEEPLINK_CHECKED, false).apply()
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun getOrCreateDeviceId(prefs: SharedPreferences): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
}

