package com.hedvig.notificationService.service.event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.utils.extractStreetName
import com.hedvig.productPricingObjects.enums.Carrier
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventName")
@JsonSubTypes(
    JsonSubTypes.Type(value = ChargeFailedEvent::class, name = "ChargeFailedEvent"),
    JsonSubTypes.Type(value = ContractCreatedEvent::class, name = "ContractCreatedEvent"),
    JsonSubTypes.Type(value = ContractRenewalQueuedEvent::class, name = "ContractRenewalQueuedEvent"),
    JsonSubTypes.Type(value = QuoteCreatedEvent::class, name = "QuoteCreatedEvent"),
    JsonSubTypes.Type(value = StartDateUpdatedEvent::class, name = "StartDateUpdatedEvent"),
    JsonSubTypes.Type(value = ContractTerminatedEvent::class, name = "ContractTerminatedEvent"),
    JsonSubTypes.Type(value = PhoneNumberUpdatedEvent::class, name = "PhoneNumberUpdatedEvent")
)
sealed class EventRequest

data class ChargeFailedEvent(
    val terminationDate: LocalDate?,
    val numberOfFailedCharges: Int?,
    val chargesLeftBeforeTermination: Int?,
    val chargeFailedReason: ChargeFailedReason,
    val memberId: String
) : EventRequest() {
    fun toMap() = mapOf(
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
) : EventRequest()

data class ContractTerminatedEvent(
    val contractId: String,
    val owningMemberId: String,
    val terminationDate: LocalDate
) : EventRequest()

data class ContractRenewalQueuedEvent(
    val contractId: String,
    val contractType: String,
    val memberId: String,
    val renewalQueuedAt: LocalDate,
    val carrierWillBeSwitched: Boolean?,
    val currentCarrier: String?,
    val carrierOnRenewal: String?
) : EventRequest() {
    fun toMap() = mapOf(
        "name" to "ContractRenewalQueuedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "contract_id" to contractId,
            "contract_type" to contractType,
            "renewal_queued_at" to renewalQueuedAt,
            "carrier_will_be_switched" to carrierWillBeSwitched,
            "current_carrier" to currentCarrier,
            "carrier_on_renewal" to carrierOnRenewal
        )
    )
}

data class QuoteCreatedEvent(
    val memberId: String,
    val quoteId: UUID,
    val firstName: String,
    val lastName: String,
    val postalCode: String?,
    val street: String?,
    val email: String,
    val ssn: String?,
    val initiatedFrom: String,
    val attributedTo: String,
    val productType: String,
    val insuranceType: String,
    val currentInsurer: String?,
    val price: BigDecimal?,
    val currency: String,
    val originatingProductId: UUID?
) : EventRequest() {
    fun toMap(): Map<String, Any> = mapOf(
        "name" to "QuoteCreatedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "initiated_from" to initiatedFrom,
            "partner" to attributedTo,
            "product_type" to productType,
            "insurance_type" to insuranceType,
            "current_insurer" to currentInsurer,
            "price" to price,
            "currency" to currency,
            "postal_code" to postalCode,
            "street" to street,
            "street_name" to street?.extractStreetName()
        )
    )
}

data class StartDateUpdatedEvent(
    val contractId: String,
    val owningMemberId: String,
    val startDate: LocalDate,
    val carrierWillBeSwitched: Boolean?,
    val currentCarrier: Carrier?,
    val carrierOnStartDate: Carrier?
) : EventRequest() {
    fun toStartDateWithUpdatedCarrierEventMap() = mapOf(
        "name" to "StartDateWithUpdatedCarrierEvent",
        "data" to mapOf(
            "member_id" to owningMemberId,
            "contract_id" to contractId,
            "current_carrier" to currentCarrier,
            "carrier_on_start_date" to carrierOnStartDate
        )
    )
}

data class PhoneNumberUpdatedEvent(
    val memberId: String,
    val phoneNumber: String
) : EventRequest()
