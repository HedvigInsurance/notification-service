package com.hedvig.notificationService.customerio.web

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import java.time.LocalDate
import javax.validation.constraints.PositiveOrZero

data class ChargeFailedEventDto(
    @JsonProperty(required = true)
    val terminationDate: LocalDate?,
    @JsonProperty(required = true)
    @get:PositiveOrZero
    val numberOfFailedCharges: Int?,
    @JsonProperty(required = true)
    @get:PositiveOrZero
    val chargesLeftBeforeTermination: Int?,
    @JsonProperty(required = true)
    val chargeFailedReason: ChargeFailedReason
)
