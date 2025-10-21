package com.deeplink.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "deeplink")
data class DeepLinkProperties(
    var matchingWindowMs: Long = 86400000, // 24 hours
    var linkExpiryMs: Long = 2592000000,   // 30 days
    var baseUrl: String = "http://localhost:8080"
)

