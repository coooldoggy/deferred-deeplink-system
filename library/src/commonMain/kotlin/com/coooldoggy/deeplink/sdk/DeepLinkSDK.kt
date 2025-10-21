package com.coooldoggy.deeplink.sdk

import com.coooldoggy.deeplink.sdk.models.DeepLinkResult
import com.coooldoggy.deeplink.sdk.models.DeviceMatchRequest
import com.coooldoggy.deeplink.sdk.models.DeviceMatchResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Deferred Deep Link SDK
 * 
 * 앱 첫 실행 시 서버와 디바이스 매칭을 수행하여 딥링크를 가져옵니다.
 */
class DeepLinkSDK(
    private val baseUrl: String,
    private val deviceInfoProvider: DeviceInfoProvider
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }
    }
    
    /**
     * 디바이스 매칭 수행 (비동기)
     * 
     * @param deviceId 앱에서 생성한 고유 디바이스 ID
     * @param callback 결과 콜백
     */
    fun checkDeferredDeepLink(
        deviceId: String,
        callback: (DeepLinkResult) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val result = checkDeferredDeepLinkSuspend(deviceId)
                callback(result)
            } catch (e: Exception) {
                callback(DeepLinkResult.Error("Failed to check deferred deep link", e))
            }
        }
    }
    
    /**
     * 디바이스 매칭 수행 (suspend 함수)
     * 
     * @param deviceId 앱에서 생성한 고유 디바이스 ID
     * @return DeepLinkResult
     */
    suspend fun checkDeferredDeepLinkSuspend(deviceId: String): DeepLinkResult {
        return try {
            // 디바이스 정보 수집
            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            
            // 매칭 요청 생성
            val request = DeviceMatchRequest(
                deviceId = deviceId,
                userAgent = deviceInfo.userAgent,
                deviceModel = deviceInfo.deviceModel,
                osName = deviceInfo.osName,
                osVersion = deviceInfo.osVersion,
                language = deviceInfo.language,
                timezone = deviceInfo.timezone,
                screenResolution = deviceInfo.screenResolution
            )
            
            // API 호출
            val response = client.post("$baseUrl/api/v1/match") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status == HttpStatusCode.OK) {
                val matchResponse: DeviceMatchResponse = response.body()
                
                if (matchResponse.matched) {
                    DeepLinkResult.Success(matchResponse)
                } else {
                    DeepLinkResult.NoMatch
                }
            } else {
                DeepLinkResult.Error("Server returned status: ${response.status}")
            }
        } catch (e: Exception) {
            DeepLinkResult.Error("Network error", e)
        }
    }
    
    fun close() {
        client.close()
    }
}

/**
 * 플랫폼별 디바이스 정보 제공자
 */
expect class DeviceInfoProvider() {
    fun getDeviceInfo(): DeviceInfo
}

/**
 * 디바이스 정보
 */
data class DeviceInfo(
    val userAgent: String,
    val deviceModel: String?,
    val osName: String?,
    val osVersion: String?,
    val language: String?,
    val timezone: String?,
    val screenResolution: String?
)

