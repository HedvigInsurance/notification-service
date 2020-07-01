package com.hedvig.notificationService.serviceIntegration.memberService.dto

data class MemberHasSignedBeforeRequest(
    val memberId: String,
    val ssn: String?,
    val email: String
)
