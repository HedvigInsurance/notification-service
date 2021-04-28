package com.hedvig.notificationService.customerio.customerioEvents

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = NorwegianContractCreatedEvent::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = NorwegianContractCreatedEvent::class, name = "NorwegianContractCreatedEvent"),
    JsonSubTypes.Type(value = DanishContractCreatedEvent::class, name = "DanishContractCreatedEvent")
)
open class ContractCreatedEvent

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "name", visible = true)
class NorwegianContractCreatedEvent(
    val data: NorwegianData
) : ContractCreatedEvent()

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "name", visible = true)
class DanishContractCreatedEvent(
    val data: DanishData
) : ContractCreatedEvent()

abstract class Data {
    var signSource: String? = null
    var partnerCode: String? = null
}

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
class NorwegianData : Data() {

    var activationDateInnbo: String? = null

    @get:JsonProperty("is_signed_innbo")
    var isSignedInnbo: Boolean = false

    @get:JsonProperty("is_switcher_innbo")
    var isSwitcherInnbo: Boolean = false
    var switcherCompanyInnbo: String? = null

    var activationDateReise: String? = null

    @get:JsonProperty("is_signed_reise")
    var isSignedReise: Boolean = false

    @get:JsonProperty("is_switcher_reise")
    var isSwitcherReise: Boolean = false
    var switcherCompanyReise: String? = null

}

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
class DanishData : Data() {

    var activationDateHomeContent: String? = null

    @get:JsonProperty("is_signed_home_content")
    var isSignedHomeContent: Boolean = false

    var activationDateTravel: String? = null

    @get:JsonProperty("is_signed_travel")
    var isSignedTravel: Boolean = false

    var activationDateAccident: String? = null

    @get:JsonProperty("is_signed_accident")
    var isSignedAccident: Boolean = false
}

fun createNorwegianData(contracts: Collection<ContractInfo>): NorwegianData {
    var data = NorwegianData()

    contracts.forEach { contract ->
        data = data.apply {
            signSource = contract.signSource
            partnerCode = contract.partnerCode
        }
        when (contract.type) {
            AgreementType.NorwegianHomeContent ->
                data = data.apply {
                    isSignedInnbo = true
                    activationDateInnbo = contract.startDate?.toString()
                    isSwitcherInnbo = contract.switcherCompany != null
                    switcherCompanyInnbo = contract.switcherCompany
                }
            AgreementType.NorwegianTravel ->
                data = data.apply {
                    isSignedReise = true
                    activationDateReise = contract.startDate?.toString()
                    isSwitcherReise = contract.switcherCompany != null
                    switcherCompanyReise = contract.switcherCompany
                }
            else -> throw RuntimeException("Unexpected contract type ${contract.type}")
        }
    }
    return data
}

fun createDanishData(contracts: Collection<ContractInfo>): DanishData {
    var data = DanishData()

    contracts.forEach { contract ->
        when (contract.type) {
            AgreementType.DanishHomeContent ->
                data = data.apply {
                    isSignedHomeContent = true
                    activationDateHomeContent = contract.startDate?.toString()
                    signSource = contract.signSource
                    partnerCode = contract.partnerCode
                }
            AgreementType.DanishTravel ->
                data = data.apply {
                    isSignedTravel = true
                    activationDateTravel = contract.startDate?.toString()
                }
            AgreementType.DanishAccident ->
                data = data.apply {
                    isSignedAccident = true
                    activationDateAccident = contract.startDate?.toString()
                }
            else -> throw RuntimeException("Unexpected contract type ${contract.type}")
        }
    }
    return data
}

