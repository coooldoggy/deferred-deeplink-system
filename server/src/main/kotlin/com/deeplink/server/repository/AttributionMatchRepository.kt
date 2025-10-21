package com.deeplink.server.repository

import com.deeplink.server.domain.entity.AttributionMatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AttributionMatchRepository : JpaRepository<AttributionMatch, Long> {
    fun findByDeviceId(deviceId: String): Optional<AttributionMatch>
    fun countByLinkId(linkId: String): Long
}

