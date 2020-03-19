package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.serviceIntegration.productPricing.client.Market
import java.util.Locale

enum class Workspace {
    SWEDEN {
        override val requiresImplementation = true
        override val countryCode = "se"
        override val market = Market.SWEDEN
    },
    NORWAY {
        override val requiresImplementation = true
        override val countryCode = "no"
        override val market = Market.NORWAY
    },
    NOT_FOUND {
        override val requiresImplementation = false
        override val countryCode = ""
        override val market = null
    };

    abstract val requiresImplementation: Boolean
    abstract val countryCode: String
    abstract val market: Market?
}

fun getWorkspaceFromLocale(locale: Locale): Workspace {
    for (workspace in Workspace.values()) {
        if (workspace.countryCode.equals(locale.country, ignoreCase = true))
            return workspace
    }
    return Workspace.NOT_FOUND
}

fun getWorkspaceFromMarket(market: Market): Workspace {
    for (workspace in Workspace.values()) {
        if (workspace.market == market)
            return workspace
    }
    return Workspace.NOT_FOUND
}
