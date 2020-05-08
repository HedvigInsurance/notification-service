package com.hedvig.notificationService.customerio.dto

import java.time.LocalDate

data class ContractCreatedEvent(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate?
)
