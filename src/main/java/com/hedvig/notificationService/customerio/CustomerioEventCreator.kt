package com.hedvig.notificationService.customerio

interface
CustomerioEventCreator {
    fun createTmpSignedInsuranceEvent(customerioState: CustomerioState): Map<String, Any?>
}
