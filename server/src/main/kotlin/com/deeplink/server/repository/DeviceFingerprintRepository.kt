package com.deeplink.server.repository

import com.deeplink.server.domain.entity.DeviceFingerprint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DeviceFingerprintRepository : JpaRepository<DeviceFingerprint, Long> {
    fun findByFingerprintHash(fingerprintHash: String): List<DeviceFingerprint>
    
    @Query("""
        SELECT df FROM DeviceFingerprint df 
        WHERE df.ipAddress = :ipAddress 
        AND df.createdAt >= :afterTime 
        AND df.matched = false
        ORDER BY df.createdAt DESC
    """)
    fun findUnmatchedByIpAddressAfter(
        @Param("ipAddress") ipAddress: String,
        @Param("afterTime") afterTime: LocalDateTime
    ): List<DeviceFingerprint>
    
    @Query("""
        SELECT df FROM DeviceFingerprint df 
        WHERE df.createdAt >= :afterTime 
        AND df.matched = false
        ORDER BY df.createdAt DESC
    """)
    fun findUnmatchedAfter(
        @Param("afterTime") afterTime: LocalDateTime
    ): List<DeviceFingerprint>
}

