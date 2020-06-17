package com.hedvig.notificationService.customerio

import java.time.LocalDate

data class ContractInfo(
    val type: AgreementType,
    val switcherCompany: String?,
    val startDate: LocalDate?,
    val signSource: String?,
    val partnerCode: String?,
    val renewalDate: LocalDate? = null
//    ,
//    val renewalScheduled: LocalDate?
)

enum class AgreementType {
    NorwegianHomeContent {
        override val typeName: String = "innbo";
    },
    NorwegianTravel {
        override val typeName: String = "reise"
    },
    SwedishHouse {
        override val typeName: String = "swedish_house"
    },
    SwedishApartment {
        override val typeName: String = "swedish_apartment"
    };

    abstract val typeName: String
}
