package com.hedvig.notificationService.serviceIntegration.productPricing

import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoaderImpl
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ContractMarketInfo
import com.hedvig.notificationService.serviceIntegration.productPricing.client.Market
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import feign.FeignException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.http.ResponseEntity
import javax.money.Monetary

class FeignExceptionForTest(status: Int = 0) : FeignException(status, "Feign test exception with status $status")

class GetWorkspaceForMemberTest {

    @MockK
    lateinit var productPricingClient: ProductPricingClient

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun getWorkspaceForMember() {

        every { productPricingClient.getContractMarketInfo(any()) } returns ResponseEntity.ok(
            ContractMarketInfo(
                Market.NORWAY,
                Monetary.getCurrency("NOK")
            )
        )

        val cut = ContractLoaderImpl(
            productPricingClient
        )
        assertThat(cut.getWorkspaceForMember("13131")).isEqualTo(Workspace.NORWAY)
    }

    @Test
    fun SwedishMarketReturnsSwedishWorkspace() {

        every { productPricingClient.getContractMarketInfo(any()) } returns ResponseEntity.ok(
            ContractMarketInfo(
                Market.SWEDEN,
                Monetary.getCurrency("SEK")
            )
        )

        val cut = ContractLoaderImpl(
            productPricingClient
        )
        assertThat(cut.getWorkspaceForMember("13131")).isEqualTo(Workspace.SWEDEN)
    }

    @Test
    fun MarketNotFoundReturnsWorkspaceNotFound() {

        every { productPricingClient.getContractMarketInfo(any()) } throws FeignExceptionForTest(404)

        val cut = ContractLoaderImpl(
            productPricingClient
        )

        assertThat(cut.getWorkspaceForMember("12345")).isEqualTo(Workspace.NOT_FOUND)
    }

    @Test
    fun feignExceptionThrownReturnsWorkspaceNotFound() {

        every { productPricingClient.getContractMarketInfo(any()) } throws FeignExceptionForTest()

        val cut = ContractLoaderImpl(
            productPricingClient
        )

        assertThat(cut.getWorkspaceForMember("12345")).isEqualTo(Workspace.NOT_FOUND)
    }
}
