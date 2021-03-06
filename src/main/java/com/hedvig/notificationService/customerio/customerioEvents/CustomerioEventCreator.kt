package com.hedvig.notificationService.customerio.customerioEvents

import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import java.time.LocalDate

interface
CustomerioEventCreator {

    fun contractCreatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ContractCreatedEvent

    fun startDateUpdatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ContractsActivationDateUpdatedEvent?

    fun contractsActivatedTodayEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>,
        dateToday: LocalDate
    ): ContractsActivatedTodayEvent?

    fun contractsTerminatedEvent(
        allMembersContracts: List<ContractInfo>,
        terminatedContractIds: List<String>
    ): ContractsTerminatedEvent?
}
