package com.hedvig.notificationService.customerio.dto

import java.math.BigDecimal
import java.util.UUID

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
) {
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
