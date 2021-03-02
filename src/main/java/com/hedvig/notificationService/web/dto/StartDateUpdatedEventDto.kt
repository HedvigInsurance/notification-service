package com.hedvig.notificationService.web.dto

import com.hedvig.productPricingObjects.enums.Carrier
import java.time.LocalDate

data class StartDateUpdatedEventDto(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate,
    val carrierWillBeSwitched: Boolean,
    val currentCarrier: Carrier,
    val carrierOnStartDate: Carrier
)
