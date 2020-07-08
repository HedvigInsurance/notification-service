package com.hedvig.notificationService.customerio.dto

import java.time.LocalDate

class ContractRenewalQueuedEvent(
    val contractId: String,
    val memberId: String,
    val renewalQueuedAt: LocalDate
) {
    fun toMap() = mapOf(
        "name" to "ContractRenewalQueuedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "contract_id" to contractId,
            "renewal_queued_at" to renewalQueuedAt
        )
    )
}
