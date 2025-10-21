package com.deeplink.server.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "DEVICE_FINGERPRINTS", indexes = [
    Index(name = "idx_fingerprint_hash", columnList = "fingerprintHash"),
    Index(name = "idx_ip_address", columnList = "ipAddress"),
    Index(name = "idx_created_at", columnList = "createdAt")
])
data class DeviceFingerprint(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fingerprint_seq")
    @SequenceGenerator(name = "fingerprint_seq", sequenceName = "FINGERPRINT_SEQ", allocationSize = 1)
    val id: Long? = null,
    
    @Column(nullable = false, length = 36)
    val linkId: String,
    
    @Column(nullable = false, length = 64)
    val fingerprintHash: String,
    
    @Column(nullable = false, length = 45)
    val ipAddress: String,
    
    @Column(nullable = false, columnDefinition = "CLOB")
    val userAgent: String,
    
    @Column(length = 100)
    val deviceModel: String? = null,
    
    @Column(length = 50)
    val osName: String? = null,
    
    @Column(length = 50)
    val osVersion: String? = null,
    
    @Column(length = 100)
    val browserName: String? = null,
    
    @Column(length = 50)
    val browserVersion: String? = null,
    
    @Column(length = 10)
    val language: String? = null,
    
    @Column(length = 50)
    val timezone: String? = null,
    
    @Column(length = 50)
    val screenResolution: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var matched: Boolean = false
)

