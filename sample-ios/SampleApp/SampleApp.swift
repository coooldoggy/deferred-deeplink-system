import SwiftUI
import SharedLibrary  // KMP 프레임워크

@main
struct DeepLinkSampleApp: App {
    init() {
        // DeepLink SDK 초기화
        DeepLinkSDKHelper.shared.initialize(
            serverUrl: Configuration.serverUrl
        )
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

// 설정
struct Configuration {
    // 개발 환경
    static let serverUrl = "http://localhost:8080"
    
    // 프로덕션 환경
    // static let serverUrl = "https://your-production-domain.com"
    
    // 실제 디바이스 테스트용 (Mac의 로컬 IP)
    // static let serverUrl = "http://192.168.1.100:8080"
}

