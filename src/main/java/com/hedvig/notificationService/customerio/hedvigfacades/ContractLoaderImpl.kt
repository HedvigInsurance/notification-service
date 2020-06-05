package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import feign.FeignException
import org.slf4j.LoggerFactory

class ContractLoaderImpl(private val productPricingClient: ProductPricingClient) :
    ContractLoader {

    val log = LoggerFactory.getLogger(ContractLoaderImpl::class.java)

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
        val response = productPricingClient.getContractsForMember(memberId)

        return response.body.map {
            ContractInfo(
                AgreementType.valueOf(it.agreements.first()::class.java.simpleName),
                it.switchedFrom,
                it.masterInception,
                it.signSource
            )
        }
    }
}
