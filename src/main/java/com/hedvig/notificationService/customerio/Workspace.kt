package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.serviceIntegration.productPricing.client.Market
import com.neovisionaries.i18n.CountryCode
import java.time.ZoneId
import java.util.Locale

enum class Workspace {
    SWEDEN {
        override val requiresImplementation = true
        override val countryCode = CountryCode.SE
        override val market = Market.SWEDEN
        override val zoneId = ZoneId.of("Europe/Stockholm")
    },
    NORWAY {
        override val requiresImplementation = true
        override val countryCode = CountryCode.NO
        override val market = Market.NORWAY
        override val zoneId = ZoneId.of("Europe/Oslo")
    },
    DENMARK {
        override val requiresImplementation = true
        override val countryCode = CountryCode.DK
        override val market = Market.DENMARK
        override val zoneId = ZoneId.of("Europe/Copenhagen")
    },
    NOT_FOUND {
        override val requiresImplementation = false
        override val countryCode = null
        override val market = null
        override val zoneId = ZoneId.of("UTC")
    };

    abstract val requiresImplementation: Boolean
    abstract val countryCode: CountryCode?
    abstract val market: Market?
    abstract val zoneId: ZoneId

    companion object {
        fun getWorkspaceFromLocale(locale: Locale): Workspace =
            values().firstOrNull { it.countryCode == CountryCode.getByLocale(locale) } ?: NOT_FOUND

        fun getWorkspaceFromMarket(market: Market): Workspace =
            values().firstOrNull { it.market == market } ?: NOT_FOUND
    }
}
