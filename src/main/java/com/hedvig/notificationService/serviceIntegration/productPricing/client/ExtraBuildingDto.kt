package com.hedvig.notificationService.serviceIntegration.productPricing.client

import java.util.UUID

data class ExtraBuildingDto(
    val id: UUID?,
    val type: String,
    val area: Int,
    val hasWaterConnected: Boolean,
    val displayName: String?
)
