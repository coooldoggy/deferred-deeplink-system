package com.deeplink.server.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "DEEP_LINKS")
data class DeepLink(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deeplink_seq")
    @SequenceGenerator(name = "deeplink_seq", sequenceName = "DEEPLINK_SEQ", allocationSize = 1)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false, length = 36)
    val linkId: String,
    
    @Column(nullable = false, length = 2000)
    val targetUrl: String,
    
    @Column(length = 500)
    val campaignName: String? = null,
    
    @Column(length = 500)
    val campaignSource: String? = null,
    
    @Column(length = 500)
    val campaignMedium: String? = null,
    
    @Column(columnDefinition = "CLOB")
    val customData: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val expiresAt: LocalDateTime? = null,
    
    @Column(nullable = false)
    var clickCount: Long = 0,
    
    @Column(nullable = false)
    var installCount: Long = 0,
    
    @Column(nullable = false)
    var active: Boolean = true
)

