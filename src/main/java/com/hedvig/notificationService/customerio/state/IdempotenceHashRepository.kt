package com.hedvig.notificationService.customerio.state

import java.time.Instant

interface IdempotenceHashRepository {
    fun save(memberId: String, hash: String)
    fun findBefore(before: Instant): List<IdempotenceHashEntry>
    fun delete(memberId: String, hash: String)
}
