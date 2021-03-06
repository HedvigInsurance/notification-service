package com.hedvig.notificationService.customerio.customerioEvents

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CustomerioEventCreatorImpl : CustomerioEventCreator {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createTmpSignedInsuranceEvent(
        argContracts: Collection<ContractInfo>
    ): TmpSignedInsuranceEvent {
        return when {
            isAllNorwegianContracts(argContracts) -> TmpSignedInsuranceEvent(NorwegianContractCreatedEvent(argContracts).data)
            isAllDanishContracts(argContracts) -> TmpSignedInsuranceEvent(DanishContractCreatedEvent(argContracts).data)
            else -> throw RuntimeException("Unexpected contracts $argContracts")
        }
    }

    fun createContractCreatedEvent(contracts: Collection<ContractInfo>): ContractCreatedEvent {
        return when {
            isAllNorwegianContracts(contracts) -> NorwegianContractCreatedEvent(contracts)
            isAllDanishContracts(contracts) -> DanishContractCreatedEvent(contracts)
            else -> throw RuntimeException("Unexpected contracts $contracts")
        }
    }

    private fun isAllNorwegianContracts(contracts: Collection<ContractInfo>): Boolean {
        val norwegianAgreementType = listOf(AgreementType.NorwegianHomeContent, AgreementType.NorwegianTravel)
        return contracts.all { contractInfo -> norwegianAgreementType.contains(contractInfo.type) }
    }

    private fun isAllDanishContracts(contracts: Collection<ContractInfo>): Boolean {
        val danishAgreementType = listOf(AgreementType.DanishHomeContent, AgreementType.DanishAccident, AgreementType.DanishTravel)
        return contracts.all { contractInfo -> danishAgreementType.contains(contractInfo.type) }
    }

    override fun contractsTerminatedEvent(
        allMembersContracts: List<ContractInfo>,
        terminatedContractIds: List<String>
    ): ContractsTerminatedEvent? {

        val terminatedContracts = allMembersContracts
            .filter { contract -> terminatedContractIds.contains(contract.contractId.toString()) && contract.terminationDate != null }
            .map(Contract.Companion::from)

        if (terminatedContracts.isEmpty()) {
            logger.info("Tried to create ContractsTerminatedEvent for contracts with ids: $terminatedContracts but found no terminated contracts")
            return null
        }

        return ContractsTerminatedEvent(ContractsTerminatedEvent.Data(terminatedContracts))
    }

    override fun contractsActivatedTodayEvent(
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
    ): ContractCreatedEvent {
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
