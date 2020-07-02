package com.hedvig.notificationService.customerio.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import java.time.LocalDate
import javax.validation.constraints.PositiveOrZero

data class ChargeFailedEvent(
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
) {
    fun toMap(memberId: String) = mapOf(
        "name" to "ChargeFailedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "number_of_failed_charges" to numberOfFailedCharges,
            "charges_left_before_termination" to chargesLeftBeforeTermination,
            "termination_date" to terminationDate,
            "charge_failed_reason" to chargeFailedReason
        )
    )
}
