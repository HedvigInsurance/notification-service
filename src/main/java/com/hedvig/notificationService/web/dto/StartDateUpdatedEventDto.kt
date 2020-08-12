package com.hedvig.notificationService.web.dto

import java.time.LocalDate

data class StartDateUpdatedEventDto(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate
)
