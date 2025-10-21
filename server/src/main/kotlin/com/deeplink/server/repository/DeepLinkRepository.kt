package com.deeplink.server.repository

import com.deeplink.server.domain.entity.DeepLink
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeepLinkRepository : JpaRepository<DeepLink, Long> {
    fun findByLinkId(linkId: String): Optional<DeepLink>
    fun findByLinkIdAndActiveTrue(linkId: String): Optional<DeepLink>
}

