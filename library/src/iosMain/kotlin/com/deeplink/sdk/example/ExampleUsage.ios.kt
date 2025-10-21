package com.deeplink.sdk.example

import com.deeplink.sdk.DeepLinkSDKHelper
import com.deeplink.sdk.models.DeepLinkResult

/**
 * iOS 사용 예제 (Swift에서 호출)
 */

/*
Swift에서 사용 예제:

// 1. AppDelegate에서 초기화
import SharedLibrary // KMP 프레임워크

class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // DeepLink SDK 초기화
        DeepLinkSDKHelper.shared.initialize(serverUrl: "https://your-server.com")
        return true
    }
}

// 2. ViewController에서 Deferred Deep Link 확인
import SharedLibrary

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Deferred Deep Link 확인
        DeepLinkSDKHelper.shared.checkDeferredDeepLink { result in
            switch result {
            case let success as DeepLinkResult.Success:
                let response = success.response
                
                // 딥링크 매칭 성공
                print("Deep Link Matched!")
                print("Target URL: \(response.targetUrl ?? "")")
                print("Campaign: \(response.campaignName ?? "")")
                print("Match Score: \(response.matchScore ?? 0)")
                
                // 타겟 화면으로 이동
                // self.navigateToTarget(response.targetUrl)
                
            case is DeepLinkResult.NoMatch:
                // 매칭된 딥링크 없음 (일반 설치)
                print("No deep link match")
                
            case let error as DeepLinkResult.Error:
                // 에러 발생
                print("Error: \(error.message)")
                
            default:
                break
            }
        }
    }
}

// 3. Async/Await 사용 예제 (iOS 13+)
import SharedLibrary

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Task {
            let result = await DeepLinkSDKHelper.shared.checkDeferredDeepLinkSuspend()
            switch result {
            case let success as DeepLinkResult.Success:
                // 처리
                break
            default:
                // 처리
                break
            }
        }
    }
}
*/

/**
 * Kotlin/Native에서 직접 사용하는 경우
 */
fun exampleUsageFromKotlin() {
    // 초기화
    DeepLinkSDKHelper.initialize(serverUrl = "https://your-server.com")
    
    // Deferred Deep Link 확인
    DeepLinkSDKHelper.checkDeferredDeepLink { result ->
        when (result) {
            is DeepLinkResult.Success -> {
                val response = result.response
                println("Deep Link Matched!")
                println("Target URL: ${response.targetUrl}")
                println("Campaign: ${response.campaignName}")
            }
            
            is DeepLinkResult.NoMatch -> {
                println("No deep link match")
            }
            
            is DeepLinkResult.Error -> {
                println("Error: ${result.message}")
            }
        }
    }
}

