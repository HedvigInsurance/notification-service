package com.hedvig.notificationService.customerio.dto

import com.hedvig.notificationService.customerio.dto.objects.Partner
import com.hedvig.notificationService.customerio.dto.objects.ProductType
import com.hedvig.notificationService.customerio.dto.objects.QuoteInitiatedFrom
import java.math.BigDecimal
import java.util.UUID

data class QuoteCreatedEvent(
    val quoteId: UUID,
    val email: String,
    val ssn: String?,
    val initiatedFrom: QuoteInitiatedFrom,
    val attributedTo: Partner,
    val productType: ProductType,
    val currentInsurer: String?,
    val price: BigDecimal,
    val currency: String,
    val originatingProductId: UUID?,
    val address: String?
) : CustomerIOEvent {
    override fun toMap(memberId: String): Map<String, Any> = mapOf(
        "name" to "QuoteCreatedEvent",
        "data" to mapOf(
            "member_id" to memberId,
            "initiated_from" to initiatedFrom,
            "partner" to attributedTo,
            "product_type" to productType,
            "current_insurer" to currentInsurer,
            "price" to price,
            "currency" to currency,
            "address" to address
        )
    )

    fun shouldSend(): Boolean {
        return initiatedFrom != QuoteInitiatedFrom.HOPE &&
            originatingProductId == null &&
            productType != ProductType.UNKNOWN
    }
}
