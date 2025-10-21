import SwiftUI
import SharedLibrary
import Combine

class DeepLinkViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var result: DeepLinkResultType?
    @Published var logs: [LogEntry] = []
    @Published var showProductScreen = false
    @Published var matchResponse: DeviceMatchResponse?
    
    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return formatter
    }()
    
    func checkDeepLink() {
        isLoading = true
        result = nil
        addLog("Deferred Deep Link 확인 시작...", type: .info)
        
        DeepLinkSDKHelper.shared.checkDeferredDeepLink { [weak self] result in
            DispatchQueue.main.async {
                self?.handleResult(result: result)
            }
        }
    }
    
    func resetAndCheck() {
        addLog("🔄 초기화 중...", type: .info)
        DeepLinkSDKHelper.shared.resetDeferredDeepLinkCheck()
        logs.removeAll()
        result = nil
        addLog("초기화 완료. 다시 확인합니다...", type: .info)
        checkDeepLink()
    }
    
    func openTestLink() {
        let urlString = "\(Configuration.serverUrl)/d/test-link-id"
        if let url = URL(string: urlString) {
            UIApplication.shared.open(url)
            addLog("테스트 링크 열기: \(urlString)", type: .info)
        }
    }
    
    private func handleResult(result: DeepLinkResult) {
        isLoading = false
        
        switch result {
        case let success as DeepLinkResult.Success:
            let response = success.response
            self.matchResponse = response
            
            addLog("✅ 매칭 성공!", type: .success)
            addLog("Link ID: \(response.linkId ?? "")", type: .info)
            addLog("Target URL: \(response.targetUrl ?? "")", type: .info)
            addLog("Campaign: \(response.campaignName ?? "없음")", type: .info)
            
            if let score = response.matchScore {
                addLog("Match Score: \(String(format: "%.2f", score))", type: .info)
            }
            
            if let customData = response.customData {
                addLog("Custom Data:", type: .info)
                for (key, value) in customData {
                    addLog("  - \(key): \(value)", type: .info)
                }
            }
            
            self.result = .success(response)
            
            // 🔥 자동으로 해당 화면으로 이동
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                self.navigateToDeepLink(response)
            }
            
        case is DeepLinkResult.NoMatch:
            addLog("ℹ️ 매칭된 딥링크 없음 (일반 설치)", type: .info)
            self.result = .noMatch
            
        case let error as DeepLinkResult.Error:
            addLog("❌ 에러 발생: \(error.message)", type: .error)
            if let exception = error.exception {
                addLog("상세: \(exception.localizedDescription)", type: .error)
            }
            self.result = .error(error.message)
            
        default:
            addLog("❌ 알 수 없는 결과", type: .error)
            self.result = .error("Unknown result type")
        }
    }
    
    private func addLog(_ message: String, type: LogType) {
        let log = LogEntry(
            message: message,
            timestamp: dateFormatter.string(from: Date()),
            type: type
        )
        logs.append(log)
    }
    
    /**
     * Deep Link에 따라 자동으로 화면 이동
     */
    private func navigateToDeepLink(_ response: DeviceMatchResponse) {
        guard let targetUrl = response.targetUrl else { return }
        
        addLog("🚀 자동 이동: \(targetUrl)", type: .success)
        
        // URL 파싱 (예: coooldoggy://product/123)
        if targetUrl.contains("product") {
            // 상품 화면으로 이동
            showProductScreen = true
        } else if targetUrl.contains("promo") {
            // 프로모션 화면으로 이동
            addLog("프로모션 화면으로 이동 (구현 필요)", type: .info)
        } else {
            addLog("알 수 없는 URL 형식: \(targetUrl)", type: .info)
        }
    }
}

// MARK: - Models
struct LogEntry: Identifiable {
    let id = UUID()
    let message: String
    let timestamp: String
    let type: LogType
    
    var icon: String {
        switch type {
        case .info: return "ℹ️"
        case .success: return "✅"
        case .error: return "❌"
        }
    }
    
    var color: Color {
        switch type {
        case .info: return .primary
        case .success: return .green
        case .error: return .red
        }
    }
}

enum LogType {
    case info, success, error
}

enum DeepLinkResultType {
    case success(DeviceMatchResponse)
    case noMatch
    case error(String)
    
    var title: String {
        switch self {
        case .success: return "✅ 딥링크 매칭 성공"
        case .noMatch: return "일반 설치"
        case .error: return "❌ 에러 발생"
        }
    }
    
    var message: String {
        switch self {
        case .success(let response):
            return """
            Target: \(response.targetUrl ?? "-")
            Campaign: \(response.campaignName ?? "-")
            """
        case .noMatch:
            return "매칭된 딥링크가 없습니다.\n홈 화면을 표시합니다."
        case .error(let message):
            return message
        }
    }
    
    var icon: String {
        switch self {
        case .success: return "checkmark.circle.fill"
        case .noMatch: return "info.circle.fill"
        case .error: return "xmark.circle.fill"
        }
    }
    
    var color: Color {
        switch self {
        case .success: return .green
        case .noMatch: return .blue
        case .error: return .red
        }
    }
    
    var backgroundColor: Color {
        switch self {
        case .success: return Color.green.opacity(0.1)
        case .noMatch: return Color.blue.opacity(0.1)
        case .error: return Color.red.opacity(0.1)
        }
    }
    
    var response: DeviceMatchResponse? {
        if case .success(let response) = self {
            return response
        }
        return nil
    }
}

