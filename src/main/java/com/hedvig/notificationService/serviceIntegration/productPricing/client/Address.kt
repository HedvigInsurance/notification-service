package com.hedvig.notificationService.serviceIntegration.productPricing.client

import com.neovisionaries.i18n.CountryCode
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class Address(
    val street: String,
    val coLine: String? = null,
    val postalCode: String,
    val city: String? = null,
    @Enumerated(EnumType.STRING)
    val country: CountryCode
)
