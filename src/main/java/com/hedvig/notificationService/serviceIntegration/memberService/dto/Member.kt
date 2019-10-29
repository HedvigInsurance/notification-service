package com.hedvig.notificationService.serviceIntegration.memberService.dto

import java.time.LocalDate

data class Member(
    val memberId: Long,
    val ssn: String?,
    val firstName: String?,
    val lastName: String?,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val floor: Int?,
    val email: String?,
    val phoneNumber: String?,
    val country: String?,
    val birthDate: LocalDate?,
    val apartment: String?,
    val acceptLanguage: String?
)
