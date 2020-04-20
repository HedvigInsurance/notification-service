package com.hedvig.notificationService.serviceIntegration.productPricing.client

enum class NorwegianHomeContentLineOfBusiness(val isYouth: Boolean) {
    RENT(isYouth = false),
    OWN(isYouth = false),
    YOUTH_RENT(isYouth = true),
    YOUTH_OWN(isYouth = true)
}
