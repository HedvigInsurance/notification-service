package com.hedvig.notificationService.customerio

interface
CustomerioEventCreator {
    fun createTmpSignedInsuranceEvent(): Map<String, Any?>
}
