package com.hedvig.notificationService.customerio.customerioEvents

import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState

interface
CustomerioEventCreator {
    fun createTmpSignedInsuranceEvent(
        customerioState: CustomerioState,
        argContracts: Collection<ContractInfo>
    ): TmpSignedInsuranceEvent

    fun createContractCreatedEvent(
        customerioState: CustomerioState,
        contracts: Collection<ContractInfo>
    ): NorwegianContractCreatedEvent

    fun execute(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult
}
