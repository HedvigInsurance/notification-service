package com.hedvig.notificationService.customerio.events

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.state.CustomerioState
import java.time.format.DateTimeFormatter

class CustomerioEventCreatorImpl(private val productPricingFacade: ProductPricingFacade) : CustomerioEventCreator {
    override fun createTmpSignedInsuranceEvent(
        customerioState: CustomerioState,
        argContracts: Collection<ContractInfo>
    ): Map<String, Any?> {

        val returnMap = mutableMapOf<String, Any?>("name" to "TmpSignedInsuranceEvent")
        createData(returnMap, argContracts)

        return returnMap.toMap()
    }

    private fun createData(
        returnMap: MutableMap<String, Any?>,
        contracts: Collection<ContractInfo>
    ) {
        val data = mutableMapOf<String, Any?>()
        returnMap["data"] = data
        contracts.forEach { contract ->

            val type = when (contract.type) {
                AgreementType.NorwegianHomeContent -> "innbo"
                AgreementType.NorwegianTravel -> "reise"
                else -> throw RuntimeException("Unexpected contract type ${contract.type}")
            }

            data["is_signed_$type"] = true
            updateActivationDate(contract, data, type)
            updateSwitcherInfo(contract, data, type)
        }
    }

    override fun contractCreatedEvent(
        customerioState: CustomerioState,
        contracts: Collection<ContractInfo>
    ): Map<String, Any?> {
        val returnMap = mutableMapOf<String, Any?>("name" to "NorwegianContractCreatedEvent")
        createData(returnMap, contracts)
        return returnMap.toMap()
    }

    override fun execute(
        customerioState: CustomerioState,
        contracts: List<ContractInfo>
    ): Pair<Map<String, Any?>, CustomerioState> {
        return when {
            customerioState.underwriterFirstSignAttributesUpdate != null -> this.createTmpSignedInsuranceEvent(
                customerioState,
                contracts
            ) to customerioState.copy(sentTmpSignEvent = true)
            customerioState.contractCreatedAt != null -> this.contractCreatedEvent(
                customerioState,
                contracts
            ) to customerioState.copy(contractCreatedAt = null)
            else -> throw RuntimeException("CustomerioState in weird state")
        }
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
