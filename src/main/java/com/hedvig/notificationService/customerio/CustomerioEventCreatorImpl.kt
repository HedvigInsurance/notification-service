package com.hedvig.notificationService.customerio

class CustomerioEventCreatorImpl : CustomerioEventCreator {
    override fun createTmpSignedInsuranceEvent(): Map<String, Any?> {
        return mapOf("name" to "TmpSignedInsuranceEvent")
    }
}
