package com.hedvig.notificationService.web.dto

import javax.money.MonetaryAmount

data class ClaimPaidNotificationRequest(
    val amount: MonetaryAmount
)
