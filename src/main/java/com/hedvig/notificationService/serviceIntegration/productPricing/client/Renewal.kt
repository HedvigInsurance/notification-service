package com.hedvig.notificationService.serviceIntegration.productPricing.client

import java.time.LocalDate
import java.util.UUID

data class Renewal(
    val renewalDate: LocalDate,
    val draftCertificateUrl: String?,
    val draftOfAgreementId: UUID?
)
