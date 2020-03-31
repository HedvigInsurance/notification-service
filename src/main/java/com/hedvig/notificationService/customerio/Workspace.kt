package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.serviceIntegration.productPricing.client.Market
import com.neovisionaries.i18n.CountryCode
import java.util.Locale

enum class Workspace {
    SWEDEN {
        override val requiresImplementation = true
        override val countryCode = CountryCode.SE
        override val market = Market.SWEDEN
    },
    NORWAY {
        override val requiresImplementation = true
        override val countryCode = CountryCode.NO
        override val market = Market.NORWAY
    },
    NOT_FOUND {
        override val requiresImplementation = false
        override val countryCode = null
        override val market = null
    };

    abstract val requiresImplementation: Boolean
    abstract val countryCode: CountryCode?
    abstract val market: Market?

    companion object {
        fun getWorkspaceFromLocale(locale: Locale): Workspace =
            values().firstOrNull { it.countryCode == CountryCode.getByLocale(locale) } ?: NOT_FOUND

        fun getWorkspaceFromMarket(market: Market): Workspace =
            values().firstOrNull() { it.market == market }
                ?: NOT_FOUND
    }
}
