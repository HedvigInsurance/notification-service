//TODO move this package with the event handler
package com.hedvig.notificationService.customerio.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.PositiveOrZero

sealed class RequestEvent

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
) : RequestEvent() {
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


data class ContractCreatedEvent(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate?,
    val signSource: String? = null
) : RequestEvent()

class ContractRenewalQueuedEvent(
    val contractId: String,
    val contractType: String,
    val memberId: String,
    val renewalQueuedAt: LocalDate
): RequestEvent() {
    fun toMap() = mapOf(
        "name" to "ContractRenewalQueuedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "contract_id" to contractId,
            "contract_type" to contractType,
            "renewal_queued_at" to renewalQueuedAt
        )
    )
}


data class QuoteCreatedEvent(
    val memberId: String,
    val quoteId: UUID,
    val firstName: String,
    val lastName: String,
    val postalCode: String?,
    val email: String,
    val ssn: String?,
    val initiatedFrom: String,
    val attributedTo: String,
    val productType: String,
    val currentInsurer: String?,
    val price: BigDecimal?,
    val currency: String,
    val originatingProductId: UUID?
): RequestEvent() {
    fun toMap(): Map<String, Any> = mapOf(
        "name" to "QuoteCreatedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "initiated_from" to initiatedFrom,
            "partner" to attributedTo,
            "product_type" to productType,
            "current_insurer" to currentInsurer,
            "price" to price,
            "currency" to currency,
            "postal_code" to postalCode
        )
    )
}

data class StartDateUpdatedEvent(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate
): RequestEvent()



