package com.hedvig.notificationService.customerio.state

import java.time.Instant

data class ContractState(
    val contractId: String,
    var contractRenewalQueuedTriggerAt: Instant? = null
)
