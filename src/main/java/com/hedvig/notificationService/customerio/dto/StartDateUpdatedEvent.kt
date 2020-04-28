package com.hedvig.notificationService.customerio.dto

data class StartDateUpdatedEvent(
    val contractId: String,
    val owningMemberId: String
)
