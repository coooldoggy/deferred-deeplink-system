package com.deeplink.server.util

import ua_parser.Parser
import java.security.MessageDigest

object FingerprintUtil {
    private val parser = Parser()
    
    /**
     * 디바이스 핑거프린트 해시 생성
     * IP + UserAgent의 주요 정보를 조합하여 해시 생성
     */
    fun generateFingerprintHash(
        ipAddress: String,
        userAgent: String,
        language: String?,
        timezone: String?,
        screenResolution: String?
    ): String {
        val client = parser.parse(userAgent)
        
        // 핑거프린트 구성 요소
        val components = listOfNotNull(
            ipAddress,
            client.os?.family,
            client.os?.major,
            client.device?.family,
            language,
            timezone,
            screenResolution
        ).joinToString("|")
        
        return sha256(components)
    }
    
    /**
     * User-Agent 파싱
     */
    fun parseUserAgent(userAgent: String): ParsedUserAgent {
        val client = parser.parse(userAgent)
        
        return ParsedUserAgent(
            osName = client.os?.family,
            osVersion = listOfNotNull(
                client.os?.major,
                client.os?.minor,
                client.os?.patch
            ).joinToString(".").takeIf { it.isNotEmpty() },
            browserName = client.userAgent?.family,
            browserVersion = listOfNotNull(
                client.userAgent?.major,
                client.userAgent?.minor
            ).joinToString(".").takeIf { it.isNotEmpty() },
            deviceModel = client.device?.family?.takeIf { it != "Other" }
        )
    }
    
    /**
     * 두 핑거프린트 간의 유사도 계산
     * 0.0 ~ 1.0 사이의 값 (높을수록 유사)
     */
    fun calculateMatchScore(
        fp1: FingerprintData,
        fp2: FingerprintData,
        timeDiffMs: Long,
        maxTimeWindowMs: Long
    ): Double {
        var score = 0.0
        var weights = 0.0
        
        // IP 주소 매칭 (가중치: 40%)
        if (fp1.ipAddress == fp2.ipAddress) {
            score += 0.4
        }
        weights += 0.4
        
        // OS 매칭 (가중치: 20%)
        if (fp1.osName != null && fp2.osName != null) {
            if (fp1.osName == fp2.osName) {
                score += 0.2
                // OS 버전까지 동일하면 추가 점수
                if (fp1.osVersion == fp2.osVersion) {
                    score += 0.05
                    weights += 0.05
                }
            }
        }
        weights += 0.2
        
        // 디바이스 모델 매칭 (가중치: 15%)
        if (fp1.deviceModel != null && fp2.deviceModel != null) {
            if (fp1.deviceModel == fp2.deviceModel) {
                score += 0.15
            }
        }
        weights += 0.15
        
        // Language 매칭 (가중치: 10%)
        if (fp1.language != null && fp2.language != null) {
            if (fp1.language == fp2.language) {
                score += 0.1
            }
        }
        weights += 0.1
        
        // Timezone 매칭 (가중치: 10%)
        if (fp1.timezone != null && fp2.timezone != null) {
            if (fp1.timezone == fp2.timezone) {
                score += 0.1
            }
        }
        weights += 0.1
        
        // Screen Resolution 매칭 (가중치: 5%)
        if (fp1.screenResolution != null && fp2.screenResolution != null) {
            if (fp1.screenResolution == fp2.screenResolution) {
                score += 0.05
            }
        }
        weights += 0.05
        
        // 시간 차이에 따른 패널티
        val timePenalty = (timeDiffMs.toDouble() / maxTimeWindowMs) * 0.3
        score -= timePenalty
        
        return (score / weights).coerceIn(0.0, 1.0)
    }
    
    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

data class ParsedUserAgent(
    val osName: String?,
    val osVersion: String?,
    val browserName: String?,
    val browserVersion: String?,
    val deviceModel: String?
)

data class FingerprintData(
    val ipAddress: String,
    val osName: String?,
    val osVersion: String?,
    val deviceModel: String?,
    val language: String?,
    val timezone: String?,
    val screenResolution: String?
)

