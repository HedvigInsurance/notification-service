package com.hedvig.notificationService.serviceIntegration.memberService.dto

data class HasPersonSignedBeforeRequest(
    val ssn: String?,
    val email: String
)
