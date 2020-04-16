package com.hedvig.notificationService.customerio

import java.time.format.DateTimeFormatter

class CustomerioEventCreatorImpl(private val productPricingFacade: ProductPricingFacade) : CustomerioEventCreator {
    override fun createTmpSignedInsuranceEvent(customerioState: CustomerioState): Map<String, Any?> {

        val contracts = productPricingFacade.getContractTypeForMember(customerioState.memberId)

        val returnMap = mutableMapOf<String, Any?>("name" to "TmpSignedInsuranceEvent")
        contracts.forEach { contract ->

            val type = when (contract.type) {
                AgreementType.NorwegianHomeContent -> "innbo"
                AgreementType.NorwegianTravel -> "reise"
                else -> throw RuntimeException("Unexpected contract type ${contract.type}")
            }

            returnMap["is_signed_$type"] = true
            updateActivationDate(contract, returnMap, type)
            updateSwitcherInfo(contract, returnMap, type)
        }

        return returnMap.toMap()
    }

    private fun updateSwitcherInfo(
        contract: ContractInfo,
        returnMap: MutableMap<String, Any?>,
        type: String
    ) {
        if (contract.switcherCompany != null) {
            returnMap["is_switcher_$type"] = true
            returnMap["switcher_company_$type"] = contract.switcherCompany
        }
    }

    private fun updateActivationDate(
        contract: ContractInfo,
        returnMap: MutableMap<String, Any?>,
        type: String
    ) {
        if (contract.startDate != null) {
            returnMap["activation_date_$type"] = contract.startDate.format(DateTimeFormatter.ISO_DATE)
        }
    }
}
