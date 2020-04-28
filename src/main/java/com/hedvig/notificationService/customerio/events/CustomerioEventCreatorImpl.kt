package com.hedvig.notificationService.customerio.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import java.time.format.DateTimeFormatter

class CustomerioEventCreatorImpl : CustomerioEventCreator {
    override fun createTmpSignedInsuranceEvent(
        customerioState: CustomerioState,
        argContracts: Collection<ContractInfo>
    ): Map<String, Any?> {

        val returnMap = mutableMapOf<String, Any?>("name" to "TmpSignedInsuranceEvent")
        createData(returnMap, argContracts)

        return returnMap.toMap()
    }

    private fun createData(
        returnMap: MutableMap<String, Any?>,
        contracts: Collection<ContractInfo>
    ) {
        val data = mutableMapOf<String, Any?>()
        returnMap["data"] = data
        contracts.forEach { contract ->

            val type = when (contract.type) {
                AgreementType.NorwegianHomeContent -> "innbo"
                AgreementType.NorwegianTravel -> "reise"
                else -> throw RuntimeException("Unexpected contract type ${contract.type}")
            }

            data["is_signed_$type"] = true
            updateActivationDate(contract, data, type)
            updateSwitcherInfo(contract, data, type)
        }
    }

    override fun contractCreatedEvent(
        customerioState: CustomerioState,
        contracts: Collection<ContractInfo>
    ): Map<String, Any?> {
        val returnMap = mutableMapOf<String, Any?>("name" to "NorwegianContractCreatedEvent")
        createData(returnMap, contracts)
        return returnMap.toMap()
    }

    override fun execute(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult {
        val result = when {
            customerioState.shouldSendTmpSignedEvent() -> this.createTmpSignedInsuranceEvent(
                customerioState,
                contracts
            ) to customerioState.sentTmpSignedEvent()
            customerioState.shouldSendContractCreatedEvent() -> this.contractCreatedEvent(
                customerioState,
                contracts
            ) to customerioState.sentContractCreatedEvent()
            customerioState.shouldSendStartDateUpdatedEvent() -> startDateUpdatedEvent(
                contracts
            ) to customerioState.sentStartDateUpdatedEvent()
            customerioState.shouldSendActivatesTodayEvent() ->
                mapOf<String, Any?>() to customerioState.sentActivatesTodayEvent()
            else -> throw RuntimeException("CustomerioState in weird state")
        }
        return ExecutionResult(null, result.first, result.second)
    }

    private fun updateSwitcherInfo(
        contract: ContractInfo,
        returnMap: MutableMap<String, Any?>,
        type: String
    ) {
        if (contract.switcherCompany != null) {
            returnMap["is_switcher_$type"] = true
            returnMap["switcher_company_$type"] = contract.switcherCompany
        }
    }

    private fun updateActivationDate(
        contract: ContractInfo,
        returnMap: MutableMap<String, Any?>,
        type: String
    ) {
        if (contract.startDate != null) {
            returnMap["activation_date_$type"] = contract.startDate.format(DateTimeFormatter.ISO_DATE)
        }
    }

    private fun startDateUpdatedEvent(
        contracts: Collection<ContractInfo>
    ): Map<String, Any?> {

        if (contracts.all { it.startDate == null }) {
            throw RuntimeException("Cannot create ActivationDateUpdatedEvent no contracts with start date")
        }

        val returnMap = mutableMapOf<String, Any?>(
            "name" to "ActivationDateUpdatedEvent"
        )

        val data = mutableMapOf<String, Any?>()
        returnMap["data"] = data

        val contractsWithStartDate = mutableListOf<MutableMap<String, Any?>>()
        data["contractsWithStartDate"] = contractsWithStartDate

        val contractsWithoutStartDate = mutableListOf<MutableMap<String, Any?>>()
        data["contractsWithoutStartDate"] = contractsWithoutStartDate

        contracts.forEach {
            if (it.startDate != null) {
                contractsWithStartDate.add(
                    mutableMapOf(
                        "type" to if (it.type == AgreementType.NorwegianTravel) "reise" else "innbo",
                        "startDate" to it.startDate.toString(),
                        "switcherCompany" to it.switcherCompany
                    )
                )
            } else {
                contractsWithoutStartDate.add(
                    mutableMapOf(
                        "type" to if (it.type == AgreementType.NorwegianTravel) "reise" else "innbo",
                        "switcherCompany" to it.switcherCompany
                    )
                )
            }
        }

        return returnMap.toMap()
    }
}

data class ExecutionResult(val event: Any?, val map: Map<String, Any?>, val second: CustomerioState) {

    val first: Map<String, Any?>
        get() {
            if (event == null) {
                return map!!
            }
            return objectMapper.convertValue(event, Map::class.java)!! as Map<String, Any?>
        }

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()
    }
}
