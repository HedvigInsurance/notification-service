package com.hedvig.notificationService.customerio.dto

data class ContractCreatedEvent(
    val contractId: String,
    val owningMemberId: String
)
