package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.serviceIntegration.productPricing.client.Contract
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import com.hedvig.notificationService.serviceIntegration.underwriter.UnderwriterClient
import feign.FeignException
import org.slf4j.LoggerFactory

class ContractLoaderImpl(
    private val productPricingClient: ProductPricingClient,
    private val underwriterClient: UnderwriterClient
) : ContractLoader {

    private val log = LoggerFactory.getLogger(ContractLoaderImpl::class.java)

    override fun getWorkspaceForMember(memberId: String): Workspace {

        return try {
            val response = productPricingClient.getContractMarketInfo(memberId)

            val market = response.body!!.market
            Workspace.getWorkspaceFromMarket(
                market
            )
        } catch (ex: FeignException) {
            if (ex.status() != 404) {
                log.error("Could not get contractMarketInfo for member $memberId", ex)
            }

            Workspace.NOT_FOUND
        }
    }

    override fun getContractInfoForMember(memberId: String): List<ContractInfo> {
        val productPricingResponse = productPricingClient.getContractsForMember(memberId)

        return productPricingResponse.body?.map { contract ->
            val underwriterResponse = loadQuote(contract)
            ContractInfo(
                type = AgreementType.valueOf(contract.agreements.first()::class.java.simpleName),
                switcherCompany = contract.switchedFrom,
                startDate = contract.masterInception,
                signSource = contract.signSource,
                partnerCode = underwriterResponse?.attributedTo,
                renewalDate = contract.renewal?.renewalDate,
                contractId = contract.id,
                terminationDate = contract.terminationDate
            )
        } ?: emptyList()
    }

    private fun loadQuote(contract: Contract) =
        try {
            underwriterClient.getQuoteFromContractId(contract.id.toString()).body
        } catch (e: FeignException) {
            if (e.status() == 404) {
                null
            } else {
                throw e
            }
        }
}
