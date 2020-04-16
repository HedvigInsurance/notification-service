package com.hedvig.notificationService.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.Customerio
import com.hedvig.customerio.CustomerioClient
import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.customerio.MemberServiceImpl
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.ProductPricingFacadeImpl
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CustomerIOConfig() {

    @Autowired
    lateinit var configuration: ConfigurationProperties

    @Value("\${hedvig.usefakes:false}")
    var useFakes: Boolean = false

    private val okHttp: OkHttpClient = OkHttpClient()

    @Bean()
    fun productPricingFacade(productPricingClient: ProductPricingClient) =
        ProductPricingFacadeImpl(productPricingClient)

    @Bean()
    fun memberServiceFacade(memberServiceClient: MemberServiceClient) = MemberServiceImpl(memberServiceClient)

    @Bean
    fun customerioService(
        productPricingFacade: ProductPricingFacade,
        memberServiceImpl: MemberServiceImpl,
        objectMapper: ObjectMapper
    ): CustomerioService {

        val clients =
            createClients(objectMapper)

        return CustomerioService(
            WorkspaceSelector(
                productPricingFacade,
                memberServiceImpl
            ),
            InMemoryCustomerIOStateRepository(),
            *clients
        )
    }

    private fun createClients(objectMapper: ObjectMapper): Array<Pair<Workspace, CustomerioClient>> {
        return if (useFakes) {
            val customerioMock = CustomerioMock(objectMapper)
            return arrayOf(
                Workspace.SWEDEN to customerioMock,
                Workspace.NORWAY to customerioMock
            )
        } else {
            this.configuration.workspaces.map {
                it.name to Customerio(
                    it.siteId,
                    it.apiKey,
                    objectMapper,
                    okHttp
                )
            }.toTypedArray()
        }
    }
}
