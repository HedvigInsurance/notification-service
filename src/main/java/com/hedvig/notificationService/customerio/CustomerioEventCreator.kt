package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.state.CustomerioState

interface
CustomerioEventCreator {
    fun createTmpSignedInsuranceEvent(customerioState: CustomerioState): Map<String, Any?>
    fun contractSignedEvent(customerioState: CustomerioState): Map<String, Any?>
}
