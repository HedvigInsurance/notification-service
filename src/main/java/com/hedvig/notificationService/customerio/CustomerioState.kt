package com.hedvig.notificationService.customerio

import java.time.Instant

data class CustomerioState(
    val memberId: String,
    val underwriterSignAttributesStarted: Instant
)
