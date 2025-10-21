package com.deeplink.server.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude

// 딥링크 생성 요청
data class CreateDeepLinkRequest(
    val targetUrl: String,
    val campaignName: String? = null,
    val campaignSource: String? = null,
    val campaignMedium: String? = null,
    val customData: Map<String, Any>? = null,
    val expiryDays: Int? = 30
)

// 딥링크 생성 응답
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateDeepLinkResponse(
    val linkId: String,
    val shortUrl: String,
    val targetUrl: String,
    val createdAt: String,
    val expiresAt: String?
)

// 디바이스 정보 (클릭 추적용)
data class DeviceInfo(
    val ipAddress: String,
    val userAgent: String,
    val deviceModel: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val browserName: String? = null,
    val browserVersion: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val screenResolution: String? = null
)

// 디바이스 매칭 요청 (앱에서 첫 실행 시)
data class DeviceMatchRequest(
    val deviceId: String,  // 앱에서 생성한 고유 ID
    val ipAddress: String,
    val userAgent: String,
    val deviceModel: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val screenResolution: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// 디바이스 매칭 응답
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DeviceMatchResponse(
    val matched: Boolean,
    val linkId: String? = null,
    val targetUrl: String? = null,
    val campaignName: String? = null,
    val campaignSource: String? = null,
    val campaignMedium: String? = null,
    val customData: Map<String, Any>? = null,
    val matchScore: Double? = null
)

// 통계 조회 응답
data class DeepLinkStats(
    val linkId: String,
    val clickCount: Long,
    val installCount: Long,
    val conversionRate: Double
)

