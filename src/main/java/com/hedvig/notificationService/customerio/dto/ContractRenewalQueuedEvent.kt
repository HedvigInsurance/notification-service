package com.hedvig.notificationService.customerio.dto

import java.time.LocalDate

class ContractRenewalQueuedEvent(
    val contractId: String,
    val contractType: String,
    val memberId: String,
    val renewalQueuedAt: LocalDate
) {
    fun toMap() = mapOf(
        "name" to "ContractRenewalQueuedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "contract_id" to contractId,
            "contract_type" to contractType,
            "renewal_queued_at" to renewalQueuedAt
        )
    )
}
