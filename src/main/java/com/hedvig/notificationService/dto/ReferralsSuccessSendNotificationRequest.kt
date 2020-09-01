package com.hedvig.notificationService.dto

import javax.validation.constraints.NotEmpty

class ReferralsSuccessSendNotificationRequest(
    val referredName: @NotEmpty String,
    val incentiveAmount: @NotEmpty String,
    val incentiveCurrency: @NotEmpty String
)
