package com.hedvig.notificationService.web.dto

import java.time.LocalDate

data class ContractRenewalQueuedEventDto(
    val contractId: String,
    val contractType: String,
    val memberId: String,
    val renewalQueuedAt: LocalDate,
    val carrierWillBeSwitched: Boolean,
    val currentCarrier: String,
    val carrierOnRenewal: String
)
