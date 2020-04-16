package com.hedvig.notificationService.customerio

class CustomerioEventCreatorImpl(private val productPricingFacade: ProductPricingFacade) : CustomerioEventCreator {
    override fun createTmpSignedInsuranceEvent(customerioState: CustomerioState): Map<String, Any?> {

        val contracts = productPricingFacade.getContractTypeForMember(customerioState.memberId)

        val returnMap = mutableMapOf<String, Any?>("name" to "TmpSignedInsuranceEvent")

        if (contracts.first().type == AgreementType.NorwegianHomeContent) {
            returnMap["is_signed_innbo"] = true
            returnMap["activation_date_innbo"] = null
        }

        return returnMap.toMap()
    }
}
