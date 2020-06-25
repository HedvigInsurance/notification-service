package com.hedvig.notificationService.customerio.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import javax.validation.constraints.PositiveOrZero

data class ChargeFailedEvent(
    @JsonProperty(required = true)
    @get:PositiveOrZero
    val numberOfFailedCharges: Int,
    @JsonProperty(required = true)
    val numberOfChargesLeft: Int,
    @JsonProperty(required = true)
    val terminationDate: LocalDate?,
    @JsonProperty(required = true)
    val memberId: String
)
