package com.hedvig.notificationService.customerio.events

import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState

interface
CustomerioEventCreator {
    fun createTmpSignedInsuranceEvent(
        customerioState: CustomerioState,
        argContracts: Collection<ContractInfo>
    ): Map<String, Any?>

    fun contractSignedEvent(customerioState: CustomerioState): Map<String, Any?>
}
