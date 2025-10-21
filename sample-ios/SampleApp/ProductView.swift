import SwiftUI
import SharedLibrary

struct ProductView: View {
    @Environment(\.dismiss) var dismiss
    let response: DeviceMatchResponse
    
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Spacer()
                
                // Icon
                Text("🎉")
                    .font(.system(size: 80))
                
                // Title
                Text(getProductTitle())
                    .font(.title)
                    .fontWeight(.bold)
                    .foregroundColor(.blue)
                
                // Description
                Text("딥링크를 통해 진입했습니다!")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                
                // Discount Badge
                if let discount = getDiscount() {
                    Text("할인: \(discount)%")
                        .font(.headline)
                        .foregroundColor(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.green)
                        .cornerRadius(8)
                }
                
                // Campaign Info
                VStack(alignment: .leading, spacing: 12) {
                    if let campaign = response.campaignName {
                        InfoRow(label: "Campaign", value: campaign)
                    }
                    
                    if let source = response.campaignSource {
                        InfoRow(label: "Source", value: source)
                    }
                    
                    if let medium = response.campaignMedium {
                        InfoRow(label: "Medium", value: medium)
                    }
                    
                    if let customData = response.customData, !customData.isEmpty {
                        Divider()
                        Text("Custom Data:")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        ForEach(Array(customData.keys.sorted()), id: \.self) { key in
                            if let value = customData[key] {
                                InfoRow(label: key, value: "\(value)")
                            }
                        }
                    }
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.blue.opacity(0.05))
                .cornerRadius(12)
                .padding(.horizontal)
                
                Spacer()
                
                // Close Button
                Button(action: { dismiss() }) {
                    Text("닫기")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(12)
                }
                .padding(.horizontal)
            }
            .padding()
            .navigationTitle("상품 화면")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
            }
        }
    }
    
    private func getProductTitle() -> String {
        if let productId = response.customData?["productId"] as? String {
            return "상품 #\(productId)"
        }
        return "상품 화면"
    }
    
    private func getDiscount() -> String? {
        if let discount = response.customData?["discount"] as? String {
            return discount
        }
        return nil
    }
}

struct ProductView_Previews: PreviewProvider {
    static var previews: some View {
        ProductView(
            response: DeviceMatchResponse(
                matched: true,
                linkId: "test-link",
                targetUrl: "myapp://product/123",
                campaignName: "Summer Sale",
                campaignSource: "facebook",
                campaignMedium: "ad",
                customData: ["productId": "123", "discount": "30"],
                matchScore: 0.85
            )
        )
    }
}

