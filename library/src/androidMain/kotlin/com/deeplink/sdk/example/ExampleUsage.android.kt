package com.deeplink.sdk.example

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.deeplink.sdk.DeepLinkSDKHelper
import com.deeplink.sdk.models.DeepLinkResult

/**
 * Android 사용 예제
 */

// 1. Application 클래스에서 초기화
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // DeepLink SDK 초기화
        DeepLinkSDKHelper.initialize(
            application = this,
            serverUrl = "https://your-server.com"
        )
    }
}

// 2. MainActivity에서 Deferred Deep Link 확인
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Deferred Deep Link 확인
        DeepLinkSDKHelper.checkDeferredDeepLink(this) { result ->
            when (result) {
                is DeepLinkResult.Success -> {
                    val response = result.response
                    
                    // 딥링크 매칭 성공
                    println("Deep Link Matched!")
                    println("Target URL: ${response.targetUrl}")
                    println("Campaign: ${response.campaignName}")
                    println("Match Score: ${response.matchScore}")
                    
                    // 타겟 화면으로 이동
                    // navigateToTarget(response.targetUrl)
                }
                
                is DeepLinkResult.NoMatch -> {
                    // 매칭된 딥링크 없음 (일반 설치)
                    println("No deep link match")
                }
                
                is DeepLinkResult.Error -> {
                    // 에러 발생
                    println("Error: ${result.message}")
                }
            }
        }
    }
}

// 3. Coroutine 사용 예제
/*
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val result = DeepLinkSDKHelper.checkDeferredDeepLinkSuspend(this@MainActivity)
            when (result) {
                is DeepLinkResult.Success -> {
                    // 처리
                }
                else -> {
                    // 처리
                }
            }
        }
    }
}
*/

