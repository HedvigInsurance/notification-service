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
            if (contract.type == AgreementType.SwedishApartment || contract.type == AgreementType.SwedishHouse) {
                throw RuntimeException("Unexpected contract type ${contract.type}")
            }
            val type = contract.type.typeName

            data["is_signed_$type"] = true
            updateActivationDate(contract, data, type)
            updateSwitcherInfo(contract, data, type)
        }
    }

    override fun createContractCreatedEvent(
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
            customerioState.shouldSendTmpSignedEvent() -> ExecutionResult(
                null, this.createTmpSignedInsuranceEvent(
                    customerioState,
                    contracts
                ), customerioState.sentTmpSignedEvent()
            )
            customerioState.shouldSendContractCreatedEvent() -> ExecutionResult(
                null, this.createContractCreatedEvent(
                    customerioState,
                    contracts
                ), customerioState.sentContractCreatedEvent()
            )
            customerioState.shouldSendStartDateUpdatedEvent() -> ExecutionResult(
                this.createStartDateUpdatedEvent(
                    contracts
                ), null, customerioState.sentStartDateUpdatedEvent()
            )
            customerioState.shouldSendActivatesTodayEvent() ->
                ExecutionResult(
                    createActivationDateTodayEvent(customerioState, contracts),
                    null,
                    customerioState.sentActivatesTodayEvent(nextActivationDate = contracts.map { it.startDate }
                        .sortedBy { it }
                        .firstOrNull { it?.isAfter(customerioState.activationDateTriggerAt) == true })
                )
            else
            -> throw RuntimeException("CustomerioState in weird state")
        }
        return result
    }

    private fun createActivationDateTodayEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ActivationDateTodayEvent {
        val contractsWithActivationDateToday =
            contracts.filter { it.startDate == customerioState.activationDateTriggerAt }
        if (contractsWithActivationDateToday.isEmpty()) {
            throw RuntimeException("Cannot send crete event no contracts with activation date today")
        }
        return ActivationDateTodayEvent(
            contractsWithActivationDateToday
                .map { Contract(it.type.typeName, it.switcherCompany, it.startDate?.toString()) },
            contracts.filter { it.startDate == null || it.startDate?.isAfter(customerioState.activationDateTriggerAt) }
                .map { Contract(it.type.typeName, it.switcherCompany, it.startDate?.toString()) }
        )
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

    private fun createStartDateUpdatedEvent(
        contracts: Collection<ContractInfo>
    ): ActivationDateUpdatedEvent {

        if (contracts.all { it.startDate == null }) {
            throw RuntimeException("Cannot create ActivationDateUpdatedEvent no contracts with start date")
        }

        val contractsWithStartDate = mutableListOf<Contract>()
        val contractsWithoutStartDate = mutableListOf<Contract>()

        contracts.forEach {
            if (it.startDate != null) {
                contractsWithStartDate.add(
                    Contract(
                        it.type.typeName,
                        it.switcherCompany,
                        it.startDate?.toString()
                    )
                )
            } else {
                contractsWithoutStartDate.add(
                    Contract(
                        it.type.typeName,
                        it.switcherCompany,
                        it.startDate?.toString()
                    )
                )
            }
        }

        return ActivationDateUpdatedEvent(
            ActivationDateUpdatedEvent.DataObject(
                contractsWithStartDate.toList(),
                contractsWithoutStartDate.toList()
            )
        )
    }
}

data class ExecutionResult(val event: Any?, val map: Map<String, Any?>?, val state: CustomerioState) {

    val asMap: Map<String, Any?>
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
