package com.hedvig.notificationService.customerio.dto

import java.time.LocalDate

data class ChargeFailedEvent(
    val numberOfFailedCharges: Int,
    val numberOfChargesLeft: Int,
    val terminationDate: LocalDate?
)
