package com.hedvig.notificationService.serviceIntegration.memberService.dto

data class PersonHasSignedBeforeRequest(
    val ssn: String?,
    val email: String
)
