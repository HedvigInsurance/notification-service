package com.hedvig.notificationService.customerio.customerioEvents

import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState

interface
CustomerioEventCreator {
    fun execute(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult

    fun contractCreatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult

    fun startDateUpdatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult
}
