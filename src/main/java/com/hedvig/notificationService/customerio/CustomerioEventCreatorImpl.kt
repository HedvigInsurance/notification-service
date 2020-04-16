package com.hedvig.notificationService.customerio

import java.time.format.DateTimeFormatter

class CustomerioEventCreatorImpl(private val productPricingFacade: ProductPricingFacade) : CustomerioEventCreator {
    override fun createTmpSignedInsuranceEvent(customerioState: CustomerioState): Map<String, Any?> {

        val contracts = productPricingFacade.getContractTypeForMember(customerioState.memberId)

        val returnMap = mutableMapOf<String, Any?>("name" to "TmpSignedInsuranceEvent")
        contracts.forEach { contract ->
            if (contract.type == AgreementType.NorwegianHomeContent) {
                returnMap["is_signed_innbo"] = true
                if (contract.startDate != null) {
                    returnMap["activation_date_innbo"] = contract.startDate.format(DateTimeFormatter.ISO_DATE)
                }
                if (contract.switcherCompany != null) {
                    returnMap["is_switcher_innbo"] = true
                    returnMap["switcher_company_innbo"] = contract.switcherCompany
                }
            } else {
                returnMap["is_signed_reise"] = true
                if (contract.startDate != null) {
                    returnMap["activation_date_reise"] = contract.startDate.format(DateTimeFormatter.ISO_DATE)
                }
                if (contract.switcherCompany != null) {
                    returnMap["is_switcher_reise"] = true
                    returnMap["switcher_company_reise"] = contract.switcherCompany
                }
            }
        }

        return returnMap.toMap()
    }
}
