package com.coooldoggy.deeplink.sample

import android.app.Application
import com.deeplink.sdk.DeepLinkSDKHelper

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // DeepLink SDK 초기화
        // 실제 서버 URL로 변경하세요
        DeepLinkSDKHelper.initialize(
            application = this,
            serverUrl = SERVER_URL
        )
    }
    
    companion object {
        // 테스트용: localhost
        // Android 에뮬레이터에서는 10.0.2.2가 호스트 머신의 localhost
        const val SERVER_URL = "http://10.0.2.2:8080"
        
        // 프로덕션: 실제 서버 URL 사용
        // const val SERVER_URL = "https://your-domain.com"
    }
}

