package com.hedvig.notificationService.customerio.state

import java.time.Instant

data class IdempotenceHashEntry(
    val memberId: String,
    val hash: String,
    val createdAt: Instant
)
