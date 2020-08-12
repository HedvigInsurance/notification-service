package com.hedvig.notificationService.web.dto

import java.time.LocalDate

data class ContractCreatedEventDto(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate?,
    val signSource: String? = null
)
