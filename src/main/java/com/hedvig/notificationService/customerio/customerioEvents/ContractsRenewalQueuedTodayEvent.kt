package com.hedvig.notificationService.customerio.customerioEvents

import java.time.LocalDate

data class ContractsRenewalQueuedTodayEvent(
    val renewalDate: LocalDate,
    val type: String
)
