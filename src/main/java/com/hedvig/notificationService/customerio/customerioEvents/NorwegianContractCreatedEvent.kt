package com.hedvig.notificationService.customerio.customerioEvents

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "name", visible = true)
data class NorwegianContractCreatedEvent(
    val data: Data
) {

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    data class Data(
        val activationDateInnbo: String?,
        @get:JsonProperty("is_signed_innbo")
        val isSignedInnbo: Boolean,
        @get:JsonProperty("is_switcher_innbo")
        val isSwitcherInnbo: Boolean,
        val switcherCompanyInnbo: String?,

        val activationDateReise: String?,
        @get:JsonProperty("is_signed_reise")
        val isSignedReise: Boolean,
        @get:JsonProperty("is_switcher_reise")
        val isSwitcherReise: Boolean,
        val switcherCompanyReise: String?

    )
}
