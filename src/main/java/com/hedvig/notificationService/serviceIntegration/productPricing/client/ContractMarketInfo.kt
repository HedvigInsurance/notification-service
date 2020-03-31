package com.hedvig.notificationService.serviceIntegration.productPricing.client

import javax.money.CurrencyUnit

data class ContractMarketInfo(
    val market: Market,
    val preferredCurrency: CurrencyUnit
)
