package com.hedvig.notificationService.customerio

class CustomerioEventCreatorImpl(private val productPricingFacade: ProductPricingFacade) : CustomerioEventCreator {
    override fun createTmpSignedInsuranceEvent(customerioState: CustomerioState): Map<String, Any?> {

        val contracts = productPricingFacade.getContractTypeForMember(customerioState.memberId)

        val returnMap = mutableMapOf<String, Any?>("name" to "TmpSignedInsuranceEvent")
        contracts.forEach { contract ->
            if (contract.type == AgreementType.NorwegianHomeContent) {
                returnMap["is_signed_innbo"] = true
                returnMap["activation_date_innbo"] = null
            } else {
                returnMap["is_signed_travel"] = true
                returnMap["activation_date_travel"] = null
            }

            if (contract.switcherCompany != null) {
                returnMap["is_switcher"] = true
                returnMap["switcher_company"] = contracts.first().switcherCompany
            }
        }

        return returnMap.toMap()
    }
}
