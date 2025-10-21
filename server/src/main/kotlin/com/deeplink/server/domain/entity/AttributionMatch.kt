package com.deeplink.server.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ATTRIBUTION_MATCHES", indexes = [
    Index(name = "idx_device_id", columnList = "deviceId")
])
data class AttributionMatch(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribution_seq")
    @SequenceGenerator(name = "attribution_seq", sequenceName = "ATTRIBUTION_SEQ", allocationSize = 1)
    val id: Long? = null,
    
    @Column(nullable = false, length = 36)
    val linkId: String,
    
    @Column(nullable = false, length = 36)
    val deviceId: String,
    
    @Column(nullable = false)
    val fingerprintId: Long,
    
    @Column(nullable = false)
    val matchScore: Double,
    
    @Column(nullable = false)
    val matchedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(length = 45)
    val ipAddress: String? = null,
    
    @Column(columnDefinition = "CLOB")
    val userAgent: String? = null,
    
    @Column(columnDefinition = "CLOB")
    val customData: String? = null
)

