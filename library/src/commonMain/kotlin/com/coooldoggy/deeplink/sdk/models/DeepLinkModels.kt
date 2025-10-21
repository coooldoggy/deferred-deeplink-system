package com.coooldoggy.deeplink.sdk.models

import kotlinx.serialization.Serializable

@Serializable
data class DeviceMatchRequest(
    val deviceId: String,
    val ipAddress: String = "",  // 서버에서 추출
    val userAgent: String,
    val deviceModel: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val screenResolution: String? = null,
    val timestamp: Long = 0L  // 서버에서 자동으로 현재 시간 사용
)

@Serializable
data class DeviceMatchResponse(
    val matched: Boolean,
    val linkId: String? = null,
    val targetUrl: String? = null,
    val campaignName: String? = null,
    val campaignSource: String? = null,
    val campaignMedium: String? = null,
    val customData: Map<String, String>? = null,
    val matchScore: Double? = null
)

/**
 * 딥링크 결과
 */
sealed class DeepLinkResult {
    data class Success(val response: DeviceMatchResponse) : DeepLinkResult()
    data class Error(val message: String, val exception: Throwable? = null) : DeepLinkResult()
    object NoMatch : DeepLinkResult()
}

