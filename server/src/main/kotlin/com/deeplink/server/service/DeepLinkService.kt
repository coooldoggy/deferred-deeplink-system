package com.deeplink.server.service

import com.deeplink.server.config.DeepLinkProperties
import com.deeplink.server.domain.dto.*
import com.deeplink.server.domain.entity.AttributionMatch
import com.deeplink.server.domain.entity.DeepLink
import com.deeplink.server.domain.entity.DeviceFingerprint
import com.deeplink.server.repository.AttributionMatchRepository
import com.deeplink.server.repository.DeepLinkRepository
import com.deeplink.server.repository.DeviceFingerprintRepository
import com.deeplink.server.util.FingerprintData
import com.deeplink.server.util.FingerprintUtil
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class DeepLinkService(
    private val deepLinkRepository: DeepLinkRepository,
    private val fingerprintRepository: DeviceFingerprintRepository,
    private val attributionRepository: AttributionMatchRepository,
    private val properties: DeepLinkProperties,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
    
    /**
     * 딥링크 생성
     */
    @Transactional
    fun createDeepLink(request: CreateDeepLinkRequest): CreateDeepLinkResponse {
        val linkId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val expiresAt = request.expiryDays?.let {
            now.plusDays(it.toLong())
        }
        
        val customDataJson = request.customData?.let {
            objectMapper.writeValueAsString(it)
        }
        
        val deepLink = DeepLink(
            linkId = linkId,
            targetUrl = request.targetUrl,
            campaignName = request.campaignName,
            campaignSource = request.campaignSource,
            campaignMedium = request.campaignMedium,
            customData = customDataJson,
            createdAt = now,
            expiresAt = expiresAt
        )
        
        deepLinkRepository.save(deepLink)
        
        val shortUrl = "${properties.baseUrl}/d/$linkId"
        
        logger.info("Created deep link: $linkId")
        
        return CreateDeepLinkResponse(
            linkId = linkId,
            shortUrl = shortUrl,
            targetUrl = request.targetUrl,
            createdAt = now.format(dateFormatter),
            expiresAt = expiresAt?.format(dateFormatter)
        )
    }
    
    /**
     * 링크 클릭 추적 (디바이스 핑거프린트 저장)
     */
    @Transactional
    fun trackClick(linkId: String, deviceInfo: DeviceInfo): String? {
        // 딥링크 존재 및 활성 상태 확인
        val deepLink = deepLinkRepository.findByLinkIdAndActiveTrue(linkId)
            .orElse(null) ?: return null
        
        // 만료 확인
        if (deepLink.expiresAt != null && deepLink.expiresAt.isBefore(LocalDateTime.now())) {
            logger.warn("Expired deep link: $linkId")
            return null
        }
        
        // User-Agent 파싱
        val parsed = FingerprintUtil.parseUserAgent(deviceInfo.userAgent)
        
        // 핑거프린트 해시 생성
        val fingerprintHash = FingerprintUtil.generateFingerprintHash(
            ipAddress = deviceInfo.ipAddress,
            userAgent = deviceInfo.userAgent,
            language = deviceInfo.language,
            timezone = deviceInfo.timezone,
            screenResolution = deviceInfo.screenResolution
        )
        
        // 핑거프린트 저장
        val fingerprint = DeviceFingerprint(
            linkId = linkId,
            fingerprintHash = fingerprintHash,
            ipAddress = deviceInfo.ipAddress,
            userAgent = deviceInfo.userAgent,
            deviceModel = deviceInfo.deviceModel ?: parsed.deviceModel,
            osName = deviceInfo.osName ?: parsed.osName,
            osVersion = deviceInfo.osVersion ?: parsed.osVersion,
            browserName = deviceInfo.browserName ?: parsed.browserName,
            browserVersion = deviceInfo.browserVersion ?: parsed.browserVersion,
            language = deviceInfo.language,
            timezone = deviceInfo.timezone,
            screenResolution = deviceInfo.screenResolution
        )
        
        fingerprintRepository.save(fingerprint)
        
        // 클릭 카운트 증가
        deepLink.clickCount++
        deepLinkRepository.save(deepLink)
        
        logger.info("Tracked click for link: $linkId, fingerprint: $fingerprintHash")
        
        return deepLink.targetUrl
    }
    
    /**
     * 디바이스 매칭 (앱 첫 실행 시)
     */
    @Transactional
    fun matchDevice(request: DeviceMatchRequest): DeviceMatchResponse {
        // 이미 매칭된 디바이스인지 확인
        val existingMatch = attributionRepository.findByDeviceId(request.deviceId)
        if (existingMatch.isPresent) {
            val match = existingMatch.get()
            val deepLink = deepLinkRepository.findByLinkId(match.linkId).orElse(null)
            
            return if (deepLink != null) {
                createMatchResponse(deepLink, match.matchScore, true)
            } else {
                DeviceMatchResponse(matched = false)
            }
        }
        
        // 타임윈도우 계산
        val windowStart = LocalDateTime.now()
            .minusNanos(properties.matchingWindowMs * 1_000_000)
        
        // 매칭 후보 찾기 (IP 기반)
        val candidates = fingerprintRepository.findUnmatchedByIpAddressAfter(
            ipAddress = request.ipAddress,
            afterTime = windowStart
        )
        
        if (candidates.isEmpty()) {
            logger.info("No matching candidates found for device: ${request.deviceId}")
            return DeviceMatchResponse(matched = false)
        }
        
        // 매칭 스코어 계산
        val deviceFp = FingerprintData(
            ipAddress = request.ipAddress,
            osName = request.osName,
            osVersion = request.osVersion,
            deviceModel = request.deviceModel,
            language = request.language,
            timezone = request.timezone,
            screenResolution = request.screenResolution
        )
        
        var bestMatch: Pair<DeviceFingerprint, Double>? = null
        
        for (candidate in candidates) {
            val candidateFp = FingerprintData(
                ipAddress = candidate.ipAddress,
                osName = candidate.osName,
                osVersion = candidate.osVersion,
                deviceModel = candidate.deviceModel,
                language = candidate.language,
                timezone = candidate.timezone,
                screenResolution = candidate.screenResolution
            )
            
            val timeDiff = request.timestamp - 
                candidate.createdAt.toInstant(ZoneOffset.UTC).toEpochMilli()
            
            val score = FingerprintUtil.calculateMatchScore(
                fp1 = deviceFp,
                fp2 = candidateFp,
                timeDiffMs = timeDiff,
                maxTimeWindowMs = properties.matchingWindowMs
            )
            
            if (bestMatch == null || score > bestMatch.second) {
                bestMatch = candidate to score
            }
        }
        
        // 매칭 임계값 확인 (70% 이상)
        if (bestMatch != null && bestMatch.second >= 0.7) {
            val (fingerprint, score) = bestMatch
            
            // 딥링크 조회
            val deepLink = deepLinkRepository.findByLinkId(fingerprint.linkId)
                .orElse(null) ?: return DeviceMatchResponse(matched = false)
            
            // 매칭 기록 저장
            val match = AttributionMatch(
                linkId = fingerprint.linkId,
                deviceId = request.deviceId,
                fingerprintId = fingerprint.id!!,
                matchScore = score,
                ipAddress = request.ipAddress,
                userAgent = request.userAgent,
                customData = deepLink.customData
            )
            attributionRepository.save(match)
            
            // 핑거프린트 매칭 상태 업데이트
            fingerprint.matched = true
            fingerprintRepository.save(fingerprint)
            
            // 설치 카운트 증가
            deepLink.installCount++
            deepLinkRepository.save(deepLink)
            
            logger.info("Device matched: ${request.deviceId} -> ${fingerprint.linkId}, score: $score")
            
            return createMatchResponse(deepLink, score, true)
        }
        
        logger.info("No match found for device: ${request.deviceId}, best score: ${bestMatch?.second}")
        return DeviceMatchResponse(matched = false)
    }
    
    /**
     * 딥링크 통계 조회
     */
    fun getStats(linkId: String): DeepLinkStats? {
        val deepLink = deepLinkRepository.findByLinkId(linkId).orElse(null)
            ?: return null
        
        val conversionRate = if (deepLink.clickCount > 0) {
            (deepLink.installCount.toDouble() / deepLink.clickCount) * 100
        } else {
            0.0
        }
        
        return DeepLinkStats(
            linkId = linkId,
            clickCount = deepLink.clickCount,
            installCount = deepLink.installCount,
            conversionRate = conversionRate
        )
    }
    
    private fun createMatchResponse(
        deepLink: DeepLink,
        matchScore: Double,
        matched: Boolean
    ): DeviceMatchResponse {
        val customData = deepLink.customData?.let {
            @Suppress("UNCHECKED_CAST")
            objectMapper.readValue(it, Map::class.java) as Map<String, Any>
        }
        
        return DeviceMatchResponse(
            matched = matched,
            linkId = deepLink.linkId,
            targetUrl = deepLink.targetUrl,
            campaignName = deepLink.campaignName,
            campaignSource = deepLink.campaignSource,
            campaignMedium = deepLink.campaignMedium,
            customData = customData,
            matchScore = matchScore
        )
    }
}

