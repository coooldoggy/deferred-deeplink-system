import SwiftUI
import SharedLibrary

struct ContentView: View {
    @StateObject private var viewModel = DeepLinkViewModel()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Header
                    HeaderView()
                    
                    // Progress Indicator
                    if viewModel.isLoading {
                        ProgressView()
                            .scaleEffect(1.5)
                            .padding()
                    }
                    
                    // Result Card
                    if let result = viewModel.result {
                        ResultCard(result: result)
                    }
                    
                    // Action Buttons
                    ActionButtonsView(
                        onCheckAgain: { viewModel.checkDeepLink() },
                        onReset: { viewModel.resetAndCheck() },
                        onTestLink: { viewModel.openTestLink() }
                    )
                    .disabled(viewModel.isLoading)
                    
                    // Log View
                    LogView(logs: viewModel.logs)
                }
                .padding()
            }
            .navigationTitle("Deferred Deep Link")
            .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            viewModel.checkDeepLink()
        }
        .sheet(isPresented: $viewModel.showProductScreen) {
            if let response = viewModel.matchResponse {
                ProductView(response: response)
            }
        }
    }
}

// MARK: - Header View
struct HeaderView: View {
    var body: some View {
        VStack(spacing: 8) {
            Text("🔗 Deferred Deep Link")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.blue)
            
            Text("iOS 샘플 앱")
                .font(.subheadline)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.blue.opacity(0.1))
        .cornerRadius(12)
    }
}

// MARK: - Result Card
struct ResultCard: View {
    let result: DeepLinkResultType
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: result.icon)
                    .foregroundColor(result.color)
                Text(result.title)
                    .font(.headline)
                    .foregroundColor(result.color)
            }
            
            Text(result.message)
                .font(.body)
                .foregroundColor(.secondary)
            
            if let response = result.response {
                VStack(alignment: .leading, spacing: 8) {
                    InfoRow(label: "Target", value: response.targetUrl ?? "-")
                    InfoRow(label: "Campaign", value: response.campaignName ?? "-")
                    
                    if let score = response.matchScore {
                        InfoRow(
                            label: "Score",
                            value: String(format: "%.0f%%", score * 100)
                        )
                    }
                    
                    if let customData = response.customData {
                        Text("Custom Data:")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        ForEach(Array(customData.keys.sorted()), id: \.self) { key in
                            if let value = customData[key] {
                                InfoRow(
                                    label: "  \(key)",
                                    value: "\(value)",
                                    indent: true
                                )
                            }
                        }
                    }
                }
                .padding(.top, 8)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(result.backgroundColor)
        .cornerRadius(12)
    }
}

struct InfoRow: View {
    let label: String
    let value: String
    var indent: Bool = false
    
    var body: some View {
        HStack {
            Text(label + ":")
                .font(indent ? .caption : .subheadline)
                .foregroundColor(.secondary)
            Text(value)
                .font(indent ? .caption : .subheadline)
                .fontWeight(indent ? .regular : .medium)
        }
    }
}

// MARK: - Action Buttons
struct ActionButtonsView: View {
    let onCheckAgain: () -> Void
    let onReset: () -> Void
    let onTestLink: () -> Void
    
    var body: some View {
        VStack(spacing: 12) {
            Button(action: onCheckAgain) {
                Label("다시 확인", systemImage: "arrow.clockwise")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            
            Button(action: onReset) {
                Label("초기화 후 확인", systemImage: "trash")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            
            Button(action: onTestLink) {
                Label("테스트 링크 열기", systemImage: "safari")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderless)
        }
    }
}

// MARK: - Log View
struct LogView: View {
    let logs: [LogEntry]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("로그")
                .font(.headline)
                .padding(.horizontal)
            
            ScrollView {
                VStack(alignment: .leading, spacing: 4) {
                    ForEach(logs) { log in
                        HStack(alignment: .top, spacing: 8) {
                            Text(log.icon)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(log.timestamp)
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Text(log.message)
                                    .font(.caption)
                                    .foregroundColor(log.color)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
                .padding()
            }
            .frame(maxWidth: .infinity)
            .frame(height: 300)
            .background(Color.gray.opacity(0.1))
            .cornerRadius(12)
        }
    }
}

// MARK: - Preview
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

