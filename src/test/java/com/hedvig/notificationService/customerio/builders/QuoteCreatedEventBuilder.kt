package com.hedvig.notificationService.customerio.builders

import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import java.math.BigDecimal
import java.util.UUID

data class QuoteCreatedEventBuilder(
    val memberId: String = MEMBER_ID,
    val quoteId: UUID = UUID.randomUUID(),
    val email: String = EMAIL,
    val ssn: String? = SSN,
    val initiatedFrom: String = "WEBONBOARDING",
    val attributedTo: String = "HEDVIG",
    val productType: String = "APARTMENT",
    val currentInsurer: String? = null,
    val price: BigDecimal = BigDecimal("99"),
    val currency: String = "SEK",
    val originatingProductId: UUID? = null,
    val address: String? = "Testvägen 1"
) {
    fun build() = QuoteCreatedEvent(
        memberId = memberId,
        quoteId = quoteId,
        email = email,
        ssn = ssn,
        initiatedFrom = initiatedFrom,
        attributedTo = attributedTo,
        productType = productType,
        currentInsurer = currentInsurer,
        price = price,
        currency = currency,
        originatingProductId = originatingProductId,
        address = address
    )
}
