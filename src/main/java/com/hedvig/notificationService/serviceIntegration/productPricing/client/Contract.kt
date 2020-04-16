package com.hedvig.notificationService.serviceIntegration.productPricing.client

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.money.CurrencyUnit

data class Contract(
    val id: UUID,
    val holderMemberId: String,
    val switchedFrom: String?,
    val masterInception: LocalDate?,
    val status: ContractStatus,
    @get:JsonProperty("isTerminated")
    val isTerminated: Boolean,
    val terminationDate: LocalDate?,
    val currentAgreementId: UUID,
    val hasPendingAgreement: Boolean,
    val agreements: List<Agreement>,
    val hasQueuedRenewal: Boolean,
    val preferredCurrency: CurrencyUnit,
    val market: Market,
    val signSource: String?,
    val contractTypeName: String,
    val createdAt: Instant
)
