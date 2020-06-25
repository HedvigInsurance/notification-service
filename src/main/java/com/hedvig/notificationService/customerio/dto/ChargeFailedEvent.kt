package com.hedvig.notificationService.customerio.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class ChargeFailedEvent(
    @JsonProperty(required = true)
    val numberOfFailedCharges: Int,
    @JsonProperty(required = true)
    val numberOfChargesLeft: Int,
    @JsonProperty(required = true)
    val terminationDate: LocalDate?
)
