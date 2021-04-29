package com.hedvig.notificationService.customerio

import java.time.LocalDate
import java.util.UUID

data class ContractInfo(
    val type: AgreementType,
    val switcherCompany: String? = null,
    val startDate: LocalDate? = null,
    val signSource: String? = null,
    val partnerCode: String? = null,
    val renewalDate: LocalDate? = null,
    val contractId: UUID,
    val terminationDate: LocalDate?
)

enum class AgreementType {
    NorwegianHomeContent {
        override val typeName: String = "innbo"
    },
    NorwegianTravel {
        override val typeName: String = "reise"
    },
    SwedishHouse {
        override val typeName: String = "swedish_house"
    },
    SwedishApartment {
        override val typeName: String = "swedish_apartment"
    },
    DanishHomeContent {
        override val typeName: String = "danish_home_content"
    },
    DanishTravel {
        override val typeName: String = "danish_travel"
    },
    DanishAccident {
        override val typeName: String = "danish_accident"
    };

    abstract val typeName: String
}
