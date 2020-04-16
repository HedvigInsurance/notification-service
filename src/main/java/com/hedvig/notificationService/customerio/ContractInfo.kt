package com.hedvig.notificationService.customerio

import java.time.LocalDate

data class ContractInfo(
    val type: AgreementType,
    val switcherCompany: String?,
    val startDate: LocalDate?
)

enum class AgreementType {
    NorwegianHomeContent,
    NorwegianTravel,
    SwedishHouse,
    SwedishApartment
}
