package com.deeplink.server.controller

import com.deeplink.server.domain.dto.*
import com.deeplink.server.service.DeepLinkService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/api/v1")
class DeepLinkController(
    private val deepLinkService: DeepLinkService
) {
    
    /**
     * 딥링크 생성 API
     * POST /api/v1/links
     */
    @PostMapping("/links")
    fun createDeepLink(
        @RequestBody request: CreateDeepLinkRequest
    ): ResponseEntity<CreateDeepLinkResponse> {
        val response = deepLinkService.createDeepLink(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    /**
     * 디바이스 매칭 API (앱 첫 실행 시)
     * POST /api/v1/match
     */
    @PostMapping("/match")
    fun matchDevice(
        @RequestBody request: DeviceMatchRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<DeviceMatchResponse> {
        // 실제 IP 주소 추출 (프록시 고려)
        val ipAddress = extractIpAddress(httpRequest)
        val requestWithIp = request.copy(ipAddress = ipAddress)
        
        val response = deepLinkService.matchDevice(requestWithIp)
        return ResponseEntity.ok(response)
    }
    
    /**
     * 딥링크 통계 조회 API
     * GET /api/v1/links/{linkId}/stats
     */
    @GetMapping("/links/{linkId}/stats")
    fun getStats(@PathVariable linkId: String): ResponseEntity<DeepLinkStats> {
        val stats = deepLinkService.getStats(linkId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(stats)
    }
    
    private fun extractIpAddress(request: HttpServletRequest): String {
        val headers = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        )
        
        for (header in headers) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrBlank() && ip != "unknown") {
                // X-Forwarded-For는 여러 IP가 쉼표로 구분될 수 있음
                return ip.split(",")[0].trim()
            }
        }
        
        return request.remoteAddr
    }
}

/**
 * 딥링크 리다이렉션 컨트롤러
 * 웹 브라우저에서 링크 클릭 시 처리
 */
@RestController
class DeepLinkRedirectController(
    private val deepLinkService: DeepLinkService
) {
    
    /**
     * 딥링크 클릭 처리 및 리다이렉션
     * GET /d/{linkId}
     */
    @GetMapping("/d/{linkId}")
    fun handleClick(
        @PathVariable linkId: String,
        httpRequest: HttpServletRequest
    ): RedirectView {
        // 디바이스 정보 수집
        val deviceInfo = DeviceInfo(
            ipAddress = extractIpAddress(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent") ?: "Unknown",
            language = httpRequest.getHeader("Accept-Language"),
            timezone = null,  // 브라우저에서는 서버에서 직접 얻기 어려움
            screenResolution = null  // 브라우저에서는 서버에서 직접 얻기 어려움
        )
        
        // 클릭 추적
        val targetUrl = deepLinkService.trackClick(linkId, deviceInfo)
        
        // 리다이렉션 (대상 URL 또는 앱스토어)
        val redirectUrl = targetUrl ?: getAppStoreUrl(httpRequest)
        
        return RedirectView(redirectUrl)
    }
    
    /**
     * 플랫폼별 앱스토어 URL 반환
     */
    private fun getAppStoreUrl(request: HttpServletRequest): String {
        val userAgent = request.getHeader("User-Agent")?.lowercase() ?: ""
        
        return when {
            userAgent.contains("android") -> {
                // Google Play Store
                "https://play.google.com/store/apps"
            }
            userAgent.contains("iphone") || userAgent.contains("ipad") -> {
                // Apple App Store
                "https://apps.apple.com"
            }
            else -> {
                // 기본 랜딩 페이지
                "https://example.com"
            }
        }
    }
    
    private fun extractIpAddress(request: HttpServletRequest): String {
        val headers = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        )
        
        for (header in headers) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrBlank() && ip != "unknown") {
                return ip.split(",")[0].trim()
            }
        }
        
        return request.remoteAddr
    }
}

