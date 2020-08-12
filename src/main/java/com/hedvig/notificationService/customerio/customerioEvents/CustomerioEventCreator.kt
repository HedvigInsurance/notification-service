package com.hedvig.notificationService.customerio.customerioEvents

import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import java.time.LocalDate

interface
CustomerioEventCreator {

    fun contractCreatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult

    fun startDateUpdatedEvent(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): ExecutionResult

    fun sendActivatesToday(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>,
        dateToday: LocalDate
    ): ExecutionResult
}
