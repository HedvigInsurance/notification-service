package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import feign.FeignException
import org.slf4j.LoggerFactory

class ProductPricingFacadeImpl(private val productPricingClient: ProductPricingClient) :
    ProductPricingFacade {

    val log = LoggerFactory.getLogger(ProductPricingFacadeImpl::class.java)

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

    override fun getContractTypeForMember(memberId: String): List<ContractInfo> {
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
