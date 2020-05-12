package com.hedvig.notificationService.customerio.customerioEvents

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState

class CustomerioEventCreatorImpl : CustomerioEventCreator {
    fun createTmpSignedInsuranceEvent(
        argContracts: Collection<ContractInfo>
    ): TmpSignedInsuranceEvent {

        return TmpSignedInsuranceEvent(createContractCreatedData(argContracts))
    }

    fun createContractCreatedEvent(
        contracts: Collection<ContractInfo>
    ): NorwegianContractCreatedEvent {

        val data = createContractCreatedData(contracts)

        return NorwegianContractCreatedEvent(data)
    }

    private fun createContractCreatedData(contracts: Collection<ContractInfo>): NorwegianContractCreatedEvent.Data {
        var data = NorwegianContractCreatedEvent.Data(
            null,
            isSignedInnbo = false,
            isSwitcherInnbo = false,
            switcherCompanyInnbo = null,
            activationDateReise = null,
            isSignedReise = false,
            isSwitcherReise = false,
            switcherCompanyReise = null
        )

        contracts.forEach { contract ->
            when (contract.type) {
                AgreementType.NorwegianHomeContent ->
                    data = data.copy(
                        isSignedInnbo = true,
                        activationDateInnbo = contract.startDate?.toString(),
                        isSwitcherInnbo = contract.switcherCompany != null,
                        switcherCompanyInnbo = contract.switcherCompany
                    )
                AgreementType.NorwegianTravel ->
                    data = data.copy(
                        isSignedReise = true,
                        activationDateReise = contract.startDate?.toString(),
                        isSwitcherReise = contract.switcherCompany != null,
                        switcherCompanyReise = contract.switcherCompany
                    )
                AgreementType.SwedishApartment,
                AgreementType.SwedishHouse ->
                    throw RuntimeException("Unexpected contract type ${contract.type}")
            }
        }
        return data
    }

    override fun execute(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult {
        return when {
            customerioState.shouldSendTmpSignedEvent() -> ExecutionResult(
                createTmpSignedInsuranceEvent(
                    contracts
                ), null, customerioState.sentTmpSignedEvent()
            )
            customerioState.shouldSendContractCreatedEvent()
            -> ExecutionResult(
                createContractCreatedEvent(
                    contracts
                ), null, customerioState.sentContractCreatedEvent()
            )
            customerioState.shouldSendStartDateUpdatedEvent() -> ExecutionResult(
                createStartDateUpdatedEvent(
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
                .map { Contract.from(it) },
            contracts.filter { it.startDate == null || it.startDate.isAfter(customerioState.activationDateTriggerAt) }
                .map { Contract.from(it) }
        )
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
                    Contract.from(it)
                )
            } else {
                contractsWithoutStartDate.add(
                    Contract.from(it)
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

data class ExecutionResult(val event: Any, val map: Map<String, Any?>?, val state: CustomerioState) {

    val asMap: Map<String, Any?>
        get() {
            @Suppress("UNCHECKED_CAST")
            return objectMapper.convertValue(event, Map::class.java)!! as Map<String, Any?>
        }

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()
    }
}
