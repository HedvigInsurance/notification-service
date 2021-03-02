package com.hedvig.notificationService.web.dto

import java.time.LocalDate

data class StartDateUpdatedEventDto(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate,
    val carrierWillBeSwitched: Boolean,
    val currentCarrier: String,
    val carrierOnStartDate: String
)
