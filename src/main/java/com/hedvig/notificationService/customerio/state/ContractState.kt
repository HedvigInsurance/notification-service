package com.hedvig.notificationService.customerio.state

import java.time.Instant
import java.time.LocalDate

data class ContractState(
    val contractId: String,
    var renewalDate: LocalDate? = null,
    var contractRenewalQueuedTriggerAt: Instant? = null
)
