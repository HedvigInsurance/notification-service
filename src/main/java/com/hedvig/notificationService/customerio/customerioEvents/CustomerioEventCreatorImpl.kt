package com.hedvig.notificationService.customerio.customerioEvents

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
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
            switcherCompanyReise = null,
            signSource = null,
            partnerCode = null
        )

        contracts.forEach { contract ->
            when (contract.type) {
                AgreementType.NorwegianHomeContent ->
                    data = data.copy(
                        isSignedInnbo = true,
                        activationDateInnbo = contract.startDate?.toString(),
                        isSwitcherInnbo = contract.switcherCompany != null,
                        switcherCompanyInnbo = contract.switcherCompany,
                        signSource = contract.signSource,
                        partnerCode = contract.partnerCode
                    )
                AgreementType.NorwegianTravel
                ->
                    data = data.copy(
                        isSignedReise = true,
                        activationDateReise = contract.startDate?.toString(),
                        isSwitcherReise = contract.switcherCompany != null,
                        switcherCompanyReise = contract.switcherCompany,
                        signSource = contract.signSource,
                        partnerCode = contract.partnerCode
                    )
                AgreementType.SwedishApartment,
                AgreementType.SwedishHouse ->
                    throw RuntimeException("Unexpected contract type ${contract.type}")
            }
        }
        return data
    }

    override fun sendActivatesToday(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>,
        dateToday: LocalDate
    ): ContractsActivatedTodayEvent? {
        val event = createActivationDateTodayEvent(contracts, dateToday)
        return event
    }

    override fun startDateUpdatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ContractsActivationDateUpdatedEvent? {
        val event = createStartDateUpdatedEvent(contracts)
        customerioState.sentStartDateUpdatedEvent()
        return event
    }

    override fun contractCreatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): NorwegianContractCreatedEvent {
        val event = createContractCreatedEvent(contracts)
        customerioState.sentContractCreatedEvent()
        return event
    }

    private fun createActivationDateTodayEvent(
        contracts: List<ContractInfo>,
        dateToday: LocalDate
    ): ContractsActivatedTodayEvent? {
        val contractsWithActivationDateToday =
            contracts.filter { it.startDate == dateToday }
        if (contractsWithActivationDateToday.isEmpty()) {
            return null
        }
        return ContractsActivatedTodayEvent(
            contractsWithActivationDateToday
                .map { Contract.from(it) },
            contracts.filter { it.startDate == null || it.startDate.isAfter(dateToday) }
                .map { Contract.from(it) }
        )
    }

    private fun createStartDateUpdatedEvent(
        contracts: Collection<ContractInfo>
    ): ContractsActivationDateUpdatedEvent? {

        if (contracts.all { it.startDate == null }) {
            return null
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

        return ContractsActivationDateUpdatedEvent(
            ContractsActivationDateUpdatedEvent.DataObject(
                contractsWithStartDate.toList(),
                contractsWithoutStartDate.toList()
            )
        )
    }
}

data class ExecutionResult(val event: Any) {

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()
    }
}
