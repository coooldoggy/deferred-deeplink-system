# 🔗 Deferred Deep Link 동작 원리

## 📱 전체 플로우

### 1️⃣ 사용자가 앱이 없는 상태에서 링크 클릭

```
사용자 → Facebook 광고 클릭
        ↓
     [서버 URL]
  https://your-domain.com/d/abc123
```

**서버가 하는 일:**
- 디바이스 정보 수집 (IP, User-Agent, 언어, 타임존 등)
- 데이터베이스에 저장 (링크 ID와 함께)
- 앱스토어로 리다이렉트

```kotlin
// server/controller/DeepLinkController.kt
@GetMapping("/d/{linkId}")
fun handleClick(@PathVariable linkId: String, httpRequest: HttpServletRequest) {
    // 디바이스 정보 수집
    val deviceInfo = DeviceInfo(
        ipAddress = extractIpAddress(httpRequest),
        userAgent = httpRequest.getHeader("User-Agent"),
        language = httpRequest.getHeader("Accept-Language"),
        ...
    )
    
    // 저장
    deepLinkService.trackClick(linkId, deviceInfo)
    
    // 앱스토어로 리다이렉트
    return RedirectView(getAppStoreUrl(httpRequest))
}
```

### 2️⃣ 사용자가 앱 설치

```
Play Store / App Store
        ↓
  [앱 설치 중...]
        ↓
    [설치 완료]
```

### 3️⃣ 사용자가 앱 첫 실행

```
앱 실행 → MainActivity.onCreate()
            ↓
    SDK 자동 초기화
            ↓
  서버에 매칭 요청
```

**Android (MainActivity.kt):**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 🔥 앱 시작하자마자 자동 실행
    checkDeferredDeepLink()
}

private fun checkDeferredDeepLink() {
    lifecycleScope.launch {
        // SDK가 디바이스 정보 수집 + 서버에 매칭 요청
        val result = DeepLinkSDKHelper.checkDeferredDeepLinkSuspend(this@MainActivity)
        handleResult(result)  // 결과 처리
    }
}
```

**iOS (ContentView.swift):**
```swift
var body: some View {
    NavigationView {
        // UI...
    }
    .onAppear {
        // 🔥 화면 나타나자마자 자동 실행
        viewModel.checkDeepLink()
    }
}
```

### 4️⃣ SDK가 디바이스 정보를 서버로 전송

```kotlin
// library/src/commonMain/kotlin/com/deeplink/sdk/DeepLinkSDK.kt
suspend fun checkDeferredDeepLinkSuspend(deviceId: String): DeepLinkResult {
    // 현재 디바이스 정보 수집
    val deviceInfo = deviceInfoProvider.getDeviceInfo()
    
    // 서버에 매칭 요청
    val request = DeviceMatchRequest(
        deviceId = deviceId,
        userAgent = deviceInfo.userAgent,
        deviceModel = deviceInfo.deviceModel,
        osName = deviceInfo.osName,
        ipAddress = "..." // 서버에서 자동 추출
        ...
    )
    
    // POST /api/v1/match
    val response = client.post("$baseUrl/api/v1/match") {
        setBody(request)
    }
    
    return if (response.matched) {
        DeepLinkResult.Success(response)
    } else {
        DeepLinkResult.NoMatch
    }
}
```

### 5️⃣ 서버가 확률적 매칭 수행

```kotlin
// server/service/DeepLinkService.kt
fun matchDevice(request: DeviceMatchRequest): DeviceMatchResponse {
    // 24시간 이내의 클릭 기록 조회
    val candidates = fingerprintRepository.findUnmatchedByIpAddressAfter(
        ipAddress = request.ipAddress,
        afterTime = LocalDateTime.now().minusHours(24)
    )
    
    // 각 후보와 유사도 계산
    for (candidate in candidates) {
        val score = FingerprintUtil.calculateMatchScore(
            fp1 = deviceFp,
            fp2 = candidateFp,
            timeDiffMs = timeDiff,
            maxTimeWindowMs = 24시간
        )
        
        // 70% 이상이면 매칭
        if (score >= 0.7) {
            return DeviceMatchResponse(
                matched = true,
                linkId = candidate.linkId,
                targetUrl = "coooldoggy://product/123",
                customData = mapOf("discount" to "50")
            )
        }
    }
    
    return DeviceMatchResponse(matched = false)
}
```

**매칭 알고리즘:**
- IP 주소 일치: 40%
- OS 정보 일치: 20%
- 디바이스 모델 일치: 15%
- 언어 일치: 10%
- 타임존 일치: 10%
- 해상도 일치: 5%
- 시간 차이 패널티: -30% (최대)

### 6️⃣ 앱이 매칭 결과를 받아서 처리

**✅ 매칭 성공 시:**

```kotlin
// Android
private fun handleResult(result: DeepLinkResult) {
    when (result) {
        is DeepLinkResult.Success -> {
            val response = result.response
            
            // 로그 출력
            addLog("✅ 매칭 성공!")
            addLog("Target URL: ${response.targetUrl}")
            
            // 🔥 자동으로 해당 화면으로 이동
            navigateToDeepLink(response)
        }
        ...
    }
}

private fun navigateToDeepLink(response: DeviceMatchResponse) {
    val targetUrl = response.targetUrl ?: return
    
    // URL 파싱: coooldoggy://product/123
    when {
        targetUrl.contains("product") -> {
            // 상품 화면으로 이동
            val intent = Intent(this, ProductActivity::class.java).apply {
                response.customData?.let { data ->
                    data["discount"]?.let { putExtra("discount", it) }
                }
            }
            startActivity(intent)
        }
    }
}
```

**❌ 매칭 실패 시:**

```kotlin
is DeepLinkResult.NoMatch -> {
    // 일반 설치로 간주
    // 홈 화면 그대로 유지
    addLog("ℹ️ 매칭된 딥링크 없음")
}
```

### 7️⃣ 사용자가 타겟 화면을 본다

```
ProductActivity 열림
     ↓
상품 #123 표시
할인 50% 적용
     ↓
사용자가 구매!
```

## 🎯 핵심 포인트

### ✅ 자동으로 실행되는 것
1. **앱 첫 실행 시 SDK가 자동으로 매칭 확인**
   - `MainActivity.onCreate()` → 자동 호출
   - `ContentView.onAppear()` → 자동 호출

2. **매칭 성공 시 자동으로 화면 전환**
   - `navigateToDeepLink()` 함수가 자동 호출
   - `targetUrl`에 따라 적절한 화면으로 이동

### ⚠️ 개발자가 설정해야 하는 것

1. **서버 URL 설정**
   ```kotlin
   // Android
   DeepLinkSDKHelper.initialize(
       application = this,
       serverUrl = "https://your-domain.com"  // ← 여기!
   )
   ```

2. **URL 라우팅 규칙**
   ```kotlin
   private fun navigateToDeepLink(response: DeviceMatchResponse) {
       when {
           targetUrl.contains("product") -> {
               // 상품 화면
               startActivity(Intent(this, ProductActivity::class.java))
           }
           targetUrl.contains("promo") -> {
               // 프로모션 화면
               startActivity(Intent(this, PromotionActivity::class.java))
           }
           targetUrl.contains("event") -> {
               // 이벤트 화면
               startActivity(Intent(this, EventActivity::class.java))
           }
           // 필요한 만큼 추가
       }
   }
   ```

3. **Custom Data 활용**
   ```kotlin
   response.customData?.let { data ->
       // 할인율
       val discount = data["discount"]
       
       // 상품 ID
       val productId = data["productId"]
       
       // 쿠폰 코드
       val couponCode = data["couponCode"]
       
       // Intent에 전달
       intent.putExtra("discount", discount)
       intent.putExtra("productId", productId)
       intent.putExtra("couponCode", couponCode)
   }
   ```

## 📊 실제 사용 예시

### 예시 1: Facebook 광고 → 특정 상품

```bash
# 1. 마케팅팀이 링크 생성
curl -X POST https://api.yourapp.com/api/v1/links \
  -d '{
    "targetUrl": "coooldoggy://product/summer-dress",
    "campaignName": "Summer Sale",
    "customData": {
      "productId": "summer-dress",
      "discount": "30",
      "couponCode": "SUMMER30"
    }
  }'

# 응답: https://api.yourapp.com/d/abc123

# 2. Facebook 광고에 링크 사용

# 3. 사용자가 클릭 → 앱스토어 → 설치 → 실행

# 4. 앱이 자동으로:
#    - 서머 드레스 상품 화면 열기
#    - 30% 할인 표시
#    - SUMMER30 쿠폰 자동 적용
```

### 예시 2: 친구 추천 링크

```bash
# 1. 유저 A가 친구 초대 링크 생성
targetUrl = "coooldoggy://referral/userA123"

# 2. 유저 B가 링크 클릭 → 설치 → 실행

# 3. 앱이 자동으로:
#    - userA123의 프로필 화면 열기
#    - "친구가 되었습니다!" 알림
#    - 양쪽 모두에게 리워드 지급
```

## 🔐 보안 고려사항

1. **개인정보 보호**
   - IP 주소, User-Agent는 24시간 후 삭제 권장
   - GDPR/개인정보보호법 준수

2. **부정 방지**
   - 같은 디바이스는 한 번만 매칭
   - 의심스러운 패턴 감지 (같은 IP에서 대량 매칭 등)

3. **HTTPS 필수**
   - 프로덕션에서는 반드시 HTTPS 사용

## 📚 추가 리소스

- [README.md](./README.md) - 전체 문서
- [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) - 통합 가이드
- [SAMPLE_APP_GUIDE.md](./SAMPLE_APP_GUIDE.md) - 샘플 앱 가이드

---

**이제 스토어에서 앱 설치하고 켜면 자동으로 해당 화면으로 이동합니다! 🎉**

