package com.hedvig.notificationService.serviceIntegration.underwriter

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class QuoteDto(
    val id: UUID,
    val createdAt: Instant,
    val price: BigDecimal? = null,
    val productType: ProductType,
    val state: QuoteState,
    val initiatedFrom: String,
    val attributedTo: String,
    val data: QuoteData,
    val currentInsurer: String? = null,
    val startDate: LocalDate? = null,
    val validity: Long,
    val memberId: String? = null,
    val breachedUnderwritingGuidelines: List<String>?,
    @get:JsonProperty("isComplete")
    val isComplete: Boolean,
    val originatingProductId: UUID?,
    val contractId: UUID?,
    val agreementId: UUID?,
    val dataCollectionId: UUID? = null,
    val signMethod: SignMethod
)

enum class QuoteState {
    INCOMPLETE,
    QUOTED,
    SIGNED,
    EXPIRED
}

enum class ProductType {
    APARTMENT,
    HOUSE,
    OBJECT,
    HOME_CONTENT,
    TRAVEL,
    UNKNOWN
}

enum class SignMethod {
    SWEDISH_BANK_ID,
    NORWEGIAN_BANK_ID
}
