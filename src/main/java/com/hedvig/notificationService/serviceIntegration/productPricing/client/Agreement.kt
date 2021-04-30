package com.hedvig.notificationService.serviceIntegration.productPricing.client

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.util.UUID
import javax.money.MonetaryAmount

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Agreement.SwedishApartment::class, name = "SwedishApartment"),
    JsonSubTypes.Type(value = Agreement.SwedishHouse::class, name = "SwedishHouse"),
    JsonSubTypes.Type(value = Agreement.NorwegianHomeContent::class, name = "NorwegianHomeContent"),
    JsonSubTypes.Type(value = Agreement.NorwegianTravel::class, name = "NorwegianTravel"),
    JsonSubTypes.Type(value = Agreement.DanishHomeContent::class, name = "DanishHomeContent"),
    JsonSubTypes.Type(value = Agreement.DanishTravel::class, name = "DanishTravel"),
    JsonSubTypes.Type(value = Agreement.DanishAccident::class, name = "DanishAccident")
)
sealed class Agreement {
    abstract val id: UUID
    abstract val fromDate: LocalDate?
    abstract val toDate: LocalDate?
    abstract val basePremium: MonetaryAmount
    abstract val certificateUrl: String?
    abstract val status: AgreementStatus

    data class SwedishApartment(
        override val id: UUID,
        override val fromDate: LocalDate?,
        override val toDate: LocalDate?,
        override val basePremium: MonetaryAmount,
        override val certificateUrl: String?,
        override val status: AgreementStatus,
        val lineOfBusiness: String,
        val address: Address,
        val numberCoInsured: Int,
        val squareMeters: Long
    ) : Agreement()

    data class SwedishHouse(
        override val id: UUID,
        override val fromDate: LocalDate?,
        override val toDate: LocalDate?,
        override val basePremium: MonetaryAmount,
        override val certificateUrl: String?,
        override val status: AgreementStatus,
        val address: Address,
        val numberCoInsured: Int,
        val squareMeters: Long,
        val ancillaryArea: Long,
        val yearOfConstruction: Int,
        val numberOfBathrooms: Int,
        val extraBuildings: List<ExtraBuildingDto>,
        val isSubleted: Boolean
    ) : Agreement()

    data class NorwegianHomeContent(
        override val id: UUID,
        override val fromDate: LocalDate?,
        override val toDate: LocalDate?,
        override val basePremium: MonetaryAmount,
        override val certificateUrl: String?,
        override val status: AgreementStatus,
        val lineOfBusiness: String,
        val address: Address,
        val numberCoInsured: Int,
        val squareMeters: Long
    ) : Agreement()

    data class NorwegianTravel(

        override val id: UUID,
        override val fromDate: LocalDate?,
        override val toDate: LocalDate?,
        override val basePremium: MonetaryAmount,
        override val certificateUrl: String?,
        override val status: AgreementStatus,
        val lineOfBusiness: String,
        val numberCoInsured: Int
    ) : Agreement()

    data class DanishHomeContent(
        override val id: UUID,
        override val fromDate: LocalDate?,
        override val toDate: LocalDate?,
        override val basePremium: MonetaryAmount,
        override val certificateUrl: String?,
        override val status: AgreementStatus,
        val address: Address,
        val numberCoInsured: Int,
        val squareMeters: Long,
        val lineOfBusiness: String
    ) : Agreement()

    data class DanishAccident(
        override val id: UUID,
        override val fromDate: LocalDate?,
        override val toDate: LocalDate?,
        override val basePremium: MonetaryAmount,
        override val certificateUrl: String?,
        override val status: AgreementStatus,
        val address: Address,
        val numberCoInsured: Int,
        val lineOfBusiness: String
    ) : Agreement()

    data class DanishTravel(
        override val id: UUID,
        override val fromDate: LocalDate?,
        override val toDate: LocalDate?,
        override val basePremium: MonetaryAmount,
        override val certificateUrl: String?,
        override val status: AgreementStatus,
        val address: Address,
        val numberCoInsured: Int,
        val lineOfBusiness: String
    ) : Agreement()

}
