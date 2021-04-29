package com.hedvig.notificationService.serviceIntegration.underwriter

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SwedishApartmentData::class, name = "apartment"),
    JsonSubTypes.Type(value = SwedishHouseData::class, name = "house"),
    JsonSubTypes.Type(value = NorwegianHomeContentsData::class, name = "norwegianHomeContentsData"),
    JsonSubTypes.Type(value = NorwegianTravelData::class, name = "norwegianTravelData"),
    JsonSubTypes.Type(value = DanishHomeContentsData::class, name = "danishHomeContentsData"),
    JsonSubTypes.Type(value = DanishAccidentData::class, name = "danishAccidentData"),
    JsonSubTypes.Type(value = DanishTravelData::class, name = "danishTravelData")
)
sealed class QuoteData {
    abstract val id: UUID
}

data class SwedishHouseData(
    override val id: UUID,
    val ssn: String? = null,
    val birthDate: LocalDate? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,

    val street: String? = null,
    val zipCode: String? = null,
    val city: String? = null,
    var livingSpace: Int? = null,
    var householdSize: Int? = null,
    val ancillaryArea: Int? = null,
    val yearOfConstruction: Int? = null,
    val numberOfBathrooms: Int? = null,
    @get:JvmName("getIsSubleted")
    val isSubleted: Boolean? = null,
    val floor: Int? = 0
) : QuoteData()

data class SwedishApartmentData(
    override val id: UUID,
    val ssn: String? = null,
    val birthDate: LocalDate? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,

    val street: String? = null,
    val city: String? = null,
    val zipCode: String? = null,
    val householdSize: Int? = null,
    val livingSpace: Int? = null,
    @get:JsonProperty(value = "isStudent")
    val isStudent: Boolean
) : QuoteData()

data class NorwegianHomeContentsData(
    override val id: UUID,
    val ssn: String? = null,
    val birthDate: LocalDate,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val street: String,
    val city: String?,
    val zipCode: String,
    val livingSpace: Int,
    val coInsured: Int,
    @get:JvmName("getIsYouth")
    val isYouth: Boolean
) : QuoteData()

data class NorwegianTravelData(
    override val id: UUID,
    val ssn: String? = null,
    val birthDate: LocalDate,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val coInsured: Int,
    @get:JvmName("getIsYouth")
    val isYouth: Boolean
) : QuoteData()

data class DanishHomeContentsData(
    override val id: UUID,
    val ssn: String?,
    val birthDate: LocalDate,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String? = null,
    val street: String,
    val zipCode: String,
    val bbrId: String? = null,
    val city: String?,
    val apartment: String?,
    val floor: String?,
    val livingSpace: Int,
    val coInsured: Int,
    @get:JvmName("getIsStudent")
    val isStudent: Boolean,
    val type: String
) : QuoteData()

data class DanishAccidentData(
    override val id: UUID,
    val ssn: String?,
    val birthDate: LocalDate,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String? = null,
    val street: String,
    val zipCode: String,
    val coInsured: Int,
    @get:JvmName("getIsStudent")
    val isStudent: Boolean
): QuoteData()

data class DanishTravelData(
    override val id: UUID,
    val ssn: String?,
    val birthDate: LocalDate,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String? = null,
    val street: String,
    val zipCode: String,
    val coInsured: Int,
    @get:JvmName("getIsStudent")
    val isStudent: Boolean
): QuoteData()