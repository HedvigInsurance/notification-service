package com.hedvig.notificationService.serviceIntegration.memberService.dto

data class HasSignedBeforeRequest(
    val memberId: String,
    val ssn: String?,
    val email: String
)
